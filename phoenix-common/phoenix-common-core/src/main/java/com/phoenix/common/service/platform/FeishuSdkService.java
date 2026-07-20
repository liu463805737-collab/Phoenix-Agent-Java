package com.phoenix.common.service.platform;

import cn.hutool.json.JSONObject;

import java.util.List;

public interface FeishuSdkService {
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
     * 获取企业信息（名称、企业编号）
     * @return JSONObject包含 name, display_id
     */
    JSONObject getTenantInfo();
}
