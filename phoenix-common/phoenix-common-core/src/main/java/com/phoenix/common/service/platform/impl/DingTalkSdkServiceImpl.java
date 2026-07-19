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

import jakarta.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DingTalkSdkServiceImpl implements DingTalkSdkService {

	private static final Cache<String, String> TOKEN_CACHE = CacheUtil.newTimedCache(7000 * 1000L);

	private static final Cache<String, String> TICKET_CACHE = CacheUtil.newTimedCache(7000 * 1000L);

	private Proxy httpProxy;

	private final PlatformInfoService platformInfoService;

	@PostConstruct
	public void initProxy() {
		String proxyHost = System.getenv("DINGTALK_PROXY_HOST");
		String proxyPortStr = System.getenv("DINGTALK_PROXY_PORT");
		if (StrUtil.isNotBlank(proxyHost) && StrUtil.isNotBlank(proxyPortStr)) {
			try {
				int proxyPort = Integer.parseInt(proxyPortStr);
				httpProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
				log.info("钉钉 HTTP 代理已配置: {}:{}", proxyHost, proxyPort);
			} catch (NumberFormatException e) {
				log.warn("钉钉代理端口格式错误: {}", proxyPortStr);
			}
		} else {
			// 尝试从 JVM 系统属性读取
			String sysHost = System.getProperty("http.proxyHost");
			String sysPort = System.getProperty("http.proxyPort");
			if (StrUtil.isNotBlank(sysHost) && StrUtil.isNotBlank(sysPort)) {
				try {
					httpProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(sysHost, Integer.parseInt(sysPort)));
					log.info("钉钉 HTTP 代理已配置(系统属性): {}:{}", sysHost, sysPort);
				} catch (NumberFormatException e) {
					log.warn("钉钉代理端口格式错误: {}", sysPort);
				}
			}
		}
	}

	private HttpRequest applyProxy(HttpRequest request) {
		if (httpProxy != null) {
			request.setProxy(httpProxy);
		}
		return request;
	}

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

		String url = DingTalkSyncConstants.TOKEN_URL + "?appkey=" + platformInfo.getAppKey() + "&appsecret="
				+ platformInfo.getCorpsecret();
		String respBody = applyProxy(HttpRequest.get(url)).execute().body();
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
		String respBody = applyProxy(HttpRequest.post(url).body(body)).execute().body();
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
		String respBody = applyProxy(HttpRequest.post(url).body(body)).execute().body();
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
			String respBody = applyProxy(HttpRequest.post(url).body(body)).execute().body();
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

	private String getJsApiTicket() {
		String cached = TICKET_CACHE.get(DingTalkSyncConstants.JSAPI_TICKET_CACHE_KEY);
		if (StrUtil.isNotBlank(cached)) {
			return cached;
		}

		String accessToken = getAccessToken();
		String url = DingTalkSyncConstants.JSAPI_TICKET_URL + "?access_token=" + accessToken;
		String respBody = applyProxy(HttpRequest.get(url)).execute().body();
		JSONObject resp = JSONUtil.parseObj(respBody);

		Integer errcode = resp.getInt("errcode");
		if (errcode == null || errcode != 0) {
			String errmsg = resp.getStr("errmsg");
			log.error("获取钉钉 jsapi_ticket 失败, errcode: {}, errmsg: {}", errcode, errmsg);
			throw new RuntimeException("获取钉钉 jsapi_ticket 失败: " + errmsg);
		}

		String ticket = resp.getStr("ticket");
		TICKET_CACHE.put(DingTalkSyncConstants.JSAPI_TICKET_CACHE_KEY, ticket);
		return ticket;
	}

	@Override
	public Map<String, String> getJsApiConfig(String url) {
		PlatformInfo platformInfo = platformInfoService.getEnabledByType(PlatformTypeEnm.DINGTALK.getCode());
		if (platformInfo == null) {
			throw new RuntimeException("未找到启用的钉钉平台配置");
		}

		String ticket = getJsApiTicket();
		String nonceStr = UUID.randomUUID().toString().replace("-", "");
		String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);

		String plain = "jsapi_ticket=" + ticket + "&noncestr=" + nonceStr
				+ "&timestamp=" + timeStamp + "&url=" + url;
		String signature = sha1(plain);

		Map<String, String> config = new LinkedHashMap<>();
		config.put("agentId", platformInfo.getAgentid());
		config.put("corpId", platformInfo.getCorpid());
		config.put("timeStamp", timeStamp);
		config.put("nonceStr", nonceStr);
		config.put("signature", signature);
		return config;
	}

	private String sha1(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] digest = md.digest(input.getBytes());
			StringBuilder sb = new StringBuilder();
			for (byte b : digest) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-1 algorithm not available", e);
		}
	}

}
