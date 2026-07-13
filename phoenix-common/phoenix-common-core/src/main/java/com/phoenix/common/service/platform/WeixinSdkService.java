package com.phoenix.common.service.platform;

import cn.hutool.json.JSONObject;

import java.util.List;

public interface WeixinSdkService {

	String getAccessToken();

	/**
	 * 获取部门列表
	 * @return
	 */
	List<JSONObject> getDepartments();

	/**
	 * 通过部门id获取部门下面人员信息详情列表
	 * @param departmentId 部门id
	 * @return
	 */
	List<JSONObject> getUsersByDepartmentId(String departmentId);

}
