package com.phoenix.privilege.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.privilege.entity.PrivilegeCompany;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrivilegeCompanyMapper extends BaseMapper<PrivilegeCompany> {

	@Delete("DELETE FROM tbl_privilege_company WHERE id = #{id}")
	void deletePhysicallyById(String id);

}
