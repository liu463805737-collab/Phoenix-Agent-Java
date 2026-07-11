package com.phoenix.common.service.platform.impl;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.phoenix.common.enm.PlatformTypeEnm;
import com.phoenix.common.model.platform.PlatformInfo;
import com.phoenix.common.service.platform.PlatformInfoService;
import com.phoenix.common.service.platform.WeixinSdkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class WeixinSdkServiceImpl implements WeixinSdkService {

    private static final String WECOM_TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
    private static final Cache<String, String> TOKEN_CACHE = CacheUtil.newTimedCache(7000 * 1000L);

    private final PlatformInfoService platformInfoService;

    @Override
    public List<JSONObject> getUsersByDepartmentId(String departmentId) {
        String accessToken = getAccessToken();
        String url = "https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token=" + accessToken + "&department_id=" + departmentId + "&fetch_child=1";
        String respBody = HttpRequest.get(url).execute().body();
        JSONObject resp = JSONUtil.parseObj(respBody);

        Integer errcode = resp.getInt("errcode");
        if (errcode == null || errcode != 0) {
            String errmsg = resp.getStr("errmsg");
            log.error("获取企业微信部门人员列表失败, errcode: {}, errmsg: {}", errcode, errmsg);
            throw new RuntimeException("获取企业微信部门人员列表失败: " + errmsg);
        }

        return resp.getJSONArray("userlist").toList(JSONObject.class);
    }

    @Override
    public List<JSONObject> getDepartments() {
        String accessToken = getAccessToken();
        String url = "https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token=" + accessToken;
        String respBody = HttpRequest.get(url).execute().body();
        JSONObject resp = JSONUtil.parseObj(respBody);

        Integer errcode = resp.getInt("errcode");
        if (errcode == null || errcode != 0) {
            String errmsg = resp.getStr("errmsg");
            log.error("获取企业微信部门列表失败, errcode: {}, errmsg: {}", errcode, errmsg);
            throw new RuntimeException("获取企业微信部门列表失败: " + errmsg);
        }

        return resp.getJSONArray("department").toList(JSONObject.class);
    }

    @Override
    public String getAccessToken() {
        String cached = TOKEN_CACHE.get("weixin_access_token");
        if (StrUtil.isNotBlank(cached)) {
            return cached;
        }

        PlatformInfo platformInfo = platformInfoService.getEnabledByType(PlatformTypeEnm.WEIXIN.getCode());
        if (platformInfo == null) {
            throw new RuntimeException("未找到启用的企业微信平台配置");
        }

        String url = StrUtil.format("{}?corpid={}&corpsecret={}", WECOM_TOKEN_URL, platformInfo.getCorpid(), platformInfo.getCorpsecret());
        String respBody = HttpRequest.get(url).execute().body();
        JSONObject resp = JSONUtil.parseObj(respBody);

        Integer errcode = resp.getInt("errcode");
        if (errcode == null || errcode != 0) {
            String errmsg = resp.getStr("errmsg");
            log.error("获取企业微信 access_token 失败, errcode: {}, errmsg: {}", errcode, errmsg);
            throw new RuntimeException("获取企业微信 access_token 失败: " + errmsg);
        }

        String accessToken = resp.getStr("access_token");
        TOKEN_CACHE.put("weixin_access_token", accessToken);
        return accessToken;
    }

}
