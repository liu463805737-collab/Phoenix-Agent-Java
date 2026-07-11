package com.phoenix.privilege.service.impl;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.privilege.entity.PrivilegeCompany;
import com.phoenix.privilege.entity.PrivilegeDepartment;
import com.phoenix.privilege.mapper.PrivilegeCompanyMapper;
import com.phoenix.privilege.mapper.PrivilegeDepartmentMapper;
import com.phoenix.privilege.service.IPrivilegeCompanyService;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PrivilegeCompanyServiceImpl extends ServiceImpl<PrivilegeCompanyMapper, PrivilegeCompany>
		implements IPrivilegeCompanyService {

	private final PrivilegeCompanyMapper privilegeCompanyMapper;

	private final PrivilegeDepartmentMapper privilegeDepartmentMapper;

	@Override
	public PrivilegeCompany getByCode(String code) {
		return QueryChain.of(privilegeCompanyMapper).eq(PrivilegeCompany::getCode, code).one();
	}

	@Override
	public ReturnVo<String> deleteCompanyById(String id) {
		long deptCount = QueryChain.of(privilegeDepartmentMapper)
			.eq(PrivilegeDepartment::getCompanyId, id)
			.count();
		if (deptCount > 0) {
			return ReturnVo.fail("删除失败，请先删除下级部门");
		}
		privilegeCompanyMapper.deletePhysicallyById(id);
		return ReturnVo.ok("删除成功");
	}

}
