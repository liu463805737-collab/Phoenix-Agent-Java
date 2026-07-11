package com.phoenix.privilege.service;

import com.mybatisflex.core.service.IService;
import com.phoenix.privilege.entity.PrivilegeDepartment;
import com.phoenix.privilege.vo.DepartmentTreeVO;

import java.util.List;

public interface IPrivilegeDepartmentService extends IService<PrivilegeDepartment> {

	List<PrivilegeDepartment> getByCompanyId(String companyId);

	PrivilegeDepartment getByCode(String code);

	List<PrivilegeDepartment> getByPid(String pid);

	List<DepartmentTreeVO> tree(String companyId);

	void syncFromPlatform(String companyId);

	void syncChildrenFromPlatform(String deptId);

	boolean deleteById(String id);

}
