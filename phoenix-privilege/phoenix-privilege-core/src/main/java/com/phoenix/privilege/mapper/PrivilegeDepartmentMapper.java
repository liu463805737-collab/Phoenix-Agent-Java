package com.phoenix.privilege.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.privilege.entity.PrivilegeDepartment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;

@Mapper
public interface PrivilegeDepartmentMapper extends BaseMapper<PrivilegeDepartment> {

	@Delete("DELETE FROM tbl_privilege_department WHERE id = #{id}")
	int deletePhysically(@Param("id") Serializable id);

}
