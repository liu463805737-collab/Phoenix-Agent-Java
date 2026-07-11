package com.phoenix.privilege.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.privilege.dto.PrivilegeRoleDTO;
import com.phoenix.privilege.entity.PrivilegeRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrivilegeRoleMapper extends BaseMapper<PrivilegeRole> {

	List<PrivilegeRole> selectPageByQuery(@Param("dto") PrivilegeRoleDTO dto, @Param("limit") long limit,
			@Param("offset") long offset);

	long countPageByQuery(@Param("dto") PrivilegeRoleDTO dto);

}
