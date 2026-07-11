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
public class FeishuSdkServiceImpl implements FeishuSdkService {

    private static final String FEISHU_BASE_URL = "https://open.feishu.cn/open-apis";
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

        String url = FEISHU_BASE_URL + "/auth/v3/tenant_access_token/internal";
        String body = JSONUtil.createObj()
                .set("app_id", platformInfo.getCorpid())
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
        String pageToken = null;
        boolean hasMore = true;

        while (hasMore) {
            String url = FEISHU_BASE_URL + "/contact/v3/departments?page_size=50";
            if (StrUtil.isNotBlank(pageToken)) {
                url += "&page_token=" + pageToken;
            }

            String respBody = HttpRequest.get(url)
                    .header("Authorization", "Bearer " + accessToken)
                    .execute().body();
            JSONObject resp = JSONUtil.parseObj(respBody);

            Integer code = resp.getInt("code");
            if (code == null || code != 0) {
                String msg = resp.getStr("msg");
                log.error("获取飞书部门列表失败, code: {}, msg: {}", code, msg);
                throw new RuntimeException("获取飞书部门列表失败: " + msg);
            }

            JSONObject data = resp.getJSONObject("data");
            if (data != null) {
                JSONArray items = data.getJSONArray("items");
                if (items != null) {
                    allDepts.addAll(items.toList(JSONObject.class));
                }
                hasMore = data.getBool("has_more", false);
                pageToken = data.getStr("page_token");
            } else {
                hasMore = false;
            }
        }

        return allDepts;
    }

    @Override
    public List<JSONObject> getUsersByDepartmentId(String departmentId) {
        String accessToken = getAccessToken();
        List<JSONObject> allUsers = new ArrayList<>();
        String pageToken = null;
        boolean hasMore = true;

        while (hasMore) {
            String url = FEISHU_BASE_URL + "/contact/v3/users?department_id=" + departmentId + "&page_size=50";
            if (StrUtil.isNotBlank(pageToken)) {
                url += "&page_token=" + pageToken;
            }

            String respBody = HttpRequest.get(url)
                    .header("Authorization", "Bearer " + accessToken)
                    .execute().body();
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
            } else {
                hasMore = false;
            }
        }

        return allUsers;
    }

}
