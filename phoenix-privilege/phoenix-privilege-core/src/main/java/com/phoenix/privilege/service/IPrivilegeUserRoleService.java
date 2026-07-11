package com.phoenix.privilege.service;

import com.mybatisflex.core.service.IService;
import com.phoenix.privilege.entity.PrivilegeUserRole;

import java.util.List;

public interface IPrivilegeUserRoleService extends IService<PrivilegeUserRole> {

	List<PrivilegeUserRole> getByUserId(String userId);

	List<PrivilegeUserRole> getByRoleId(String roleId);

	void batchSaveUsers(String roleId, List<String> userIds);

	void batchRemoveUsers(String roleId, List<String> userIds);

	void removeUserRoleByUserId(String userId);
	void removeUserRoleByRoleId(String roleId);

}
