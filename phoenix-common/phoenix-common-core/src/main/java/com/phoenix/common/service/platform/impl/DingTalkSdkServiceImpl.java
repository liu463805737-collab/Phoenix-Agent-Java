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
import com.phoenix.common.service.platform.DingTalkSdkService;
import com.phoenix.common.service.platform.PlatformInfoService;
import com.phoenix.common.service.sync.dingtalk.DingTalkSyncConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DingTalkSdkServiceImpl implements DingTalkSdkService {

	private static final Cache<String, String> TOKEN_CACHE = CacheUtil.newTimedCache(7000 * 1000L);

	private final PlatformInfoService platformInfoService;

	@Override
	public String getAccessToken() {
		String cached = TOKEN_CACHE.get("dingtalk_access_token");
		if (StrUtil.isNotBlank(cached)) {
			return cached;
		}

		PlatformInfo platformInfo = platformInfoService.getEnabledByType(PlatformTypeEnm.DINGTALK.getCode());
		if (platformInfo == null) {
			throw new RuntimeException("未找到启用的钉钉平台配置");
		}

		String url = DingTalkSyncConstants.TOKEN_URL + "?appkey=" + platformInfo.getCorpid() + "&appsecret="
				+ platformInfo.getCorpsecret();
		String respBody = HttpRequest.get(url).execute().body();
		JSONObject resp = JSONUtil.parseObj(respBody);

		Integer errcode = resp.getInt("errcode");
		if (errcode == null || errcode != 0) {
			String errmsg = resp.getStr("errmsg");
			log.error("获取钉钉 access_token 失败, errcode: {}, errmsg: {}", errcode, errmsg);
			throw new RuntimeException("获取钉钉 access_token 失败: " + errmsg);
		}

		String accessToken = resp.getStr("access_token");
		TOKEN_CACHE.put("dingtalk_access_token", accessToken);
		return accessToken;
	}

	@Override
	public List<JSONObject> getDepartments() {
		String accessToken = getAccessToken();
		List<JSONObject> allDepts = new ArrayList<>();
		// 先获取根部门"1"（公司本身）的信息并加入列表
		JSONObject rootDept = getDepartmentDetail(accessToken, "1");
		if (rootDept != null) {
			allDepts.add(rootDept);
		}
		// 再从根部门开始递归获取所有子部门
		fetchSubDepartments(accessToken, "1", allDepts);
		return allDepts;
	}

	private JSONObject getDepartmentDetail(String accessToken, String deptId) {
		String url = DingTalkSyncConstants.DEPT_GET_URL + "?access_token=" + accessToken;
		String body = JSONUtil.createObj().set("dept_id", deptId).toString();
		String respBody = HttpRequest.post(url).body(body).execute().body();
		JSONObject resp = JSONUtil.parseObj(respBody);
		Integer errcode = resp.getInt("errcode");
		if (errcode == null || errcode != 0) {
			log.error("获取钉钉部门详情失败, dept_id: {}, errcode: {}, errmsg: {}", deptId, errcode, resp.getStr("errmsg"));
			return null;
		}
		return resp.getJSONObject("result");
	}

	private void fetchSubDepartments(String accessToken, String deptId, List<JSONObject> result) {
		String url = DingTalkSyncConstants.DEPT_LIST_SUB_URL + "?access_token=" + accessToken;
		String body = "{\"dept_id\": " + deptId + "}";
		String respBody = HttpRequest.post(url).body(body).execute().body();
		JSONObject resp = JSONUtil.parseObj(respBody);

		Integer errcode = resp.getInt("errcode");
		if (errcode == null || errcode != 0) {
			log.error("获取钉钉子部门列表失败, dept_id: {}, errcode: {}, errmsg: {}", deptId, errcode, resp.getStr("errmsg"));
			throw new RuntimeException("获取钉钉子部门列表失败: " + resp.getStr("errmsg"));
		}

		JSONArray resultArr = resp.getJSONArray("result");
		if (resultArr != null && !resultArr.isEmpty()) {
			List<JSONObject> depts = resultArr.toList(JSONObject.class);
			result.addAll(depts);
			for (JSONObject dept : depts) {
				fetchSubDepartments(accessToken, dept.getStr("dept_id"), result);
			}
		}
	}

	@Override
	public List<JSONObject> getUsersByDepartmentId(String departmentId) {
		String accessToken = getAccessToken();
		List<JSONObject> allUsers = new ArrayList<>();
		long cursor = 0;
		boolean hasMore = true;

		while (hasMore) {
			String url = DingTalkSyncConstants.USER_LIST_URL + "?access_token=" + accessToken;
			String body = JSONUtil.createObj()
				.set("dept_id", Long.parseLong(departmentId))
				.set("cursor", cursor)
				.set("size", 100)
				.toString();
			String respBody = HttpRequest.post(url).body(body).execute().body();
			JSONObject resp = JSONUtil.parseObj(respBody);

			Integer errcode = resp.getInt("errcode");
			if (errcode == null || errcode != 0) {
				log.error("获取钉钉部门人员列表失败, dept_id: {}, errcode: {}, errmsg: {}", departmentId, errcode,
						resp.getStr("errmsg"));
				throw new RuntimeException("获取钉钉部门人员列表失败: " + resp.getStr("errmsg"));
			}

			JSONObject result = resp.getJSONObject("result");
			if (result != null) {
				JSONArray list = result.getJSONArray("list");
				if (list != null) {
					allUsers.addAll(list.toList(JSONObject.class));
				}
				hasMore = result.getBool("has_more", false);
				cursor = result.getLong("next_cursor", 0L);
			}
			else {
				hasMore = false;
			}
		}

		return allUsers;
	}

}
