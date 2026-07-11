package com.phoenix.platform.mapper.front;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.platform.model.front.AccountInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 前台账号信息 Mapper
 */
@Mapper
public interface AccountInfoMapper extends BaseMapper<AccountInfo> {

	@Delete("DELETE FROM tbl_platform_account_info WHERE id = #{id}")
	boolean deletePhysicallyById(String id);

}
