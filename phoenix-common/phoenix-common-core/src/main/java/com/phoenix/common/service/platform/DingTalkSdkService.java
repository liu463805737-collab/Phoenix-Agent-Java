package com.phoenix.common.service.platform;

import cn.hutool.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface DingTalkSdkService {
    String getAccessToken();

    /**
     * 获取部门列表
     * @return
     */
    List<JSONObject> getDepartments() ;

    /**
     * 通过部门id获取部门下面人员信息详情列表
     * @param departmentId 部门id
     * @return
     */
    List<JSONObject> getUsersByDepartmentId(String departmentId) ;

    /**
     * 获取 JSAPI 鉴权配置（用于前端 dd.config）
     * @param url 当前页面的完整 URL
     * @return agentId, corpId, timeStamp, nonceStr, signature
     */
    Map<String, String> getJsApiConfig(String url);
}
