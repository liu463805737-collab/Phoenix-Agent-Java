package com.phoenix.privilege.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.privilege.entity.PrivilegeAcl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PrivilegeAclMapper extends BaseMapper<PrivilegeAcl> {

	void deleteByReleaseId(@Param("releaseId") String releaseId);

}
