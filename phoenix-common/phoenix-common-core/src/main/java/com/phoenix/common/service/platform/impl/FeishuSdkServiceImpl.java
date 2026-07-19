package com.phoenix.common.service.platform.impl;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.phoenix.common.enm.PlatformTypeEnm;
import com.phoenix.common.model.platform.PlatformInfo;
import com.phoenix.common.service.platform.FeishuSdkService;
import com.phoenix.common.service.platform.PlatformInfoService;
import com.phoenix.common.service.sync.feishu.FeishuSyncConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FeishuSdkServiceImpl implements FeishuSdkService {

	private static final Cache<String, String> TOKEN_CACHE = CacheUtil.newTimedCache(7000 * 1000L);

	private final PlatformInfoService platformInfoService;

	@Override
	public String getAccessToken() {
		String cached = TOKEN_CACHE.get("feishu_tenant_access_token");
		if (StrUtil.isNotBlank(cached)) {
			return cached;
		}

		PlatformInfo platformInfo = platformInfoService.getEnabledByType(PlatformTypeEnm.FEISHU.getCode());
		if (platformInfo == null) {
			throw new RuntimeException("未找到启用的飞书平台配置");
		}

		String url = FeishuSyncConstants.TOKEN_URL;
		String body = JSONUtil.createObj()
			.set("app_id", platformInfo.getAppKey())
			.set("app_secret", platformInfo.getCorpsecret())
			.toString();
		String respBody = HttpRequest.post(url).body(body).execute().body();
		JSONObject resp = JSONUtil.parseObj(respBody);

		Integer code = resp.getInt("code");
		if (code == null || code != 0) {
			String msg = resp.getStr("msg");
			log.error("获取飞书 tenant_access_token 失败, code: {}, msg: {}", code, msg);
			throw new RuntimeException("获取飞书 tenant_access_token 失败: " + msg);
		}

		String accessToken = resp.getStr("tenant_access_token");
		TOKEN_CACHE.put("feishu_tenant_access_token", accessToken);
		return accessToken;
	}

	@Override
	public List<JSONObject> getDepartments() {
		String accessToken = getAccessToken();
		List<JSONObject> allDepts = new ArrayList<>();
		Set<String> fetchedIds = new HashSet<>();
		// 先获取根部门下的直接子部门（不传 parent_department_id）
		List<JSONObject> rootChildren = new ArrayList<>();
		fetchDeptPage(accessToken, null, rootChildren);
		for (JSONObject dept : rootChildren) {
			String deptId = resolveDeptId(dept);
			if (StrUtil.isNotBlank(deptId) && fetchedIds.add(deptId)) {
				allDepts.add(dept);
			}
		}
		// BFS 逐层递归获取所有层级的子部门
		List<JSONObject> currentLevel = new ArrayList<>(allDepts);
		while (!currentLevel.isEmpty()) {
			List<JSONObject> nextLevel = new ArrayList<>();
			for (JSONObject parent : currentLevel) {
				String parentId = resolveDeptId(parent);
				if (StrUtil.isBlank(parentId)) {
					continue;
				}
				List<JSONObject> children = new ArrayList<>();
				fetchDeptPage(accessToken, parentId, children);
				for (JSONObject child : children) {
					String childId = resolveDeptId(child);
					if (StrUtil.isNotBlank(childId) && fetchedIds.add(childId)) {
						allDepts.add(child);
						nextLevel.add(child);
					}
				}
			}
			currentLevel = nextLevel;
		}
		return allDepts;
	}

	/** 优先取 open_department_id，降级到 department_id */
	private static String resolveDeptId(JSONObject dept) {
		String id = dept.getStr("open_department_id");
		return StrUtil.isNotBlank(id) ? id : dept.getStr("department_id");
	}

	private void fetchDeptPage(String accessToken, String parentDeptId, List<JSONObject> result) {
		String pageToken = null;
		boolean hasMore = true;
		while (hasMore) {
			StringBuilder url = new StringBuilder(FeishuSyncConstants.DEPT_LIST_URL + "?page_size=50&department_id_type=open_department_id");
			if (StrUtil.isNotBlank(parentDeptId)) {
				url.append("&parent_department_id=").append(parentDeptId);
			}
			if (StrUtil.isNotBlank(pageToken)) {
				url.append("&page_token=").append(pageToken);
			}

			String respBody = HttpRequest.get(url.toString())
				.header("Authorization", "Bearer " + accessToken)
				.execute()
				.body();
			JSONObject resp = JSONUtil.parseObj(respBody);

			Integer code = resp.getInt("code");
			if (code == null || code != 0) {
				String msg = resp.getStr("msg");
				log.error("获取飞书部门列表失败, parentDeptId: {}, code: {}, msg: {}", parentDeptId, code, msg);
				throw new RuntimeException("获取飞书部门列表失败: " + msg);
			}

			JSONObject data = resp.getJSONObject("data");
			if (data != null) {
				JSONArray items = data.getJSONArray("items");
				if (items != null) {
					result.addAll(items.toList(JSONObject.class));
				}
				hasMore = data.getBool("has_more", false);
				pageToken = data.getStr("page_token");
			}
			else {
				hasMore = false;
			}
		}
	}

	@Override
	public JSONObject getTenantInfo() {
		String accessToken = getAccessToken();
		String respBody = HttpRequest.get(FeishuSyncConstants.TENANT_QUERY_URL)
			.header("Authorization", "Bearer " + accessToken)
			.execute()
			.body();
		JSONObject resp = JSONUtil.parseObj(respBody);
		Integer code = resp.getInt("code");
		if (code == null || code != 0) {
			String msg = resp.getStr("msg");
			log.error("获取飞书企业信息失败, code: {}, msg: {}", code, msg);
			throw new RuntimeException("获取飞书企业信息失败: " + msg);
		}
		JSONObject tenant = resp.getJSONObject("data").getJSONObject("tenant");
		if (tenant == null) {
			throw new RuntimeException("获取飞书企业信息失败: 返回数据为空");
		}
		return tenant;
	}

	@Override
	public List<JSONObject> getUsersByDepartmentId(String departmentId) {
		String accessToken = getAccessToken();
		List<JSONObject> allUsers = new ArrayList<>();
		String pageToken = null;
		boolean hasMore = true;

		while (hasMore) {
			String url = FeishuSyncConstants.USER_LIST_URL + "?department_id=" + departmentId + "&page_size=50&department_id_type=open_department_id";
			if (StrUtil.isNotBlank(pageToken)) {
				url += "&page_token=" + pageToken;
			}

			String respBody = HttpRequest.get(url).header("Authorization", "Bearer " + accessToken).execute().body();
			JSONObject resp = JSONUtil.parseObj(respBody);

			Integer code = resp.getInt("code");
			if (code == null || code != 0) {
				String msg = resp.getStr("msg");
				log.error("获取飞书部门人员列表失败, dept_id: {}, code: {}, msg: {}", departmentId, code, msg);
				throw new RuntimeException("获取飞书部门人员列表失败: " + msg);
			}

			JSONObject data = resp.getJSONObject("data");
			if (data != null) {
				JSONArray items = data.getJSONArray("items");
				if (items != null) {
					allUsers.addAll(items.toList(JSONObject.class));
				}
				hasMore = data.getBool("has_more", false);
				pageToken = data.getStr("page_token");
			}
			else {
				hasMore = false;
			}
		}

		return allUsers;
	}

}
