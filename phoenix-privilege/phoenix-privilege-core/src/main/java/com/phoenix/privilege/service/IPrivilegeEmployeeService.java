package com.phoenix.privilege.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.phoenix.privilege.dto.PrivilegeEmployeeDTO;
import com.phoenix.privilege.entity.PrivilegeEmployee;
import com.phoenix.privilege.vo.PrivilegeEmployeeVO;

public interface IPrivilegeEmployeeService extends IService<PrivilegeEmployee> {

	Page<PrivilegeEmployeeVO> page(Page<PrivilegeEmployeeVO> page, PrivilegeEmployeeDTO dto);

	PrivilegeEmployee getByEmpCode(String empCode);

	void syncFromPlatform(String companyId);

	void syncByDeptId(String deptId);

}
