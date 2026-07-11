package com.phoenix.privilege.service;

import com.mybatisflex.core.service.IService;
import com.phoenix.privilege.entity.PrivilegeCompany;
import com.phoenix.tools.vo.ReturnVo;

public interface IPrivilegeCompanyService extends IService<PrivilegeCompany> {

	PrivilegeCompany getByCode(String code);

	ReturnVo<String> deleteCompanyById(String id);

}
