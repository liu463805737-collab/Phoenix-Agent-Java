package com.phoenix.privilege.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.phoenix.privilege.dto.PrivilegeRoleDTO;
import com.phoenix.privilege.entity.PrivilegeRole;
import com.phoenix.privilege.vo.PrivilegeRoleVO;
import com.phoenix.privilege.vo.RoleAclVO;

import java.util.List;

public interface IPrivilegeRoleService extends IService<PrivilegeRole> {

	List<PrivilegeRole> getByCompanyId(Long companyId);

	List<RoleAclVO> getRoleAcls(String roleId);

	Page<PrivilegeRoleVO> pageByQuery(Page<PrivilegeRoleVO> page, PrivilegeRoleDTO dto);

	void deleteRoleById(String roleId);

}
