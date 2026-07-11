package com.phoenix.privilege.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import com.phoenix.privilege.dto.PrivilegeEmployeeDTO;
import com.phoenix.privilege.entity.PrivilegeEmployee;
import com.phoenix.privilege.vo.PrivilegeEmployeeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrivilegeEmployeeMapper extends BaseMapper<PrivilegeEmployee> {


}
