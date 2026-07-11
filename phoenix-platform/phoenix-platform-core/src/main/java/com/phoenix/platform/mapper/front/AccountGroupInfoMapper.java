package com.phoenix.platform.mapper.front;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.platform.model.front.AccountGroupInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 账号-组织关联 Mapper
 */
@Mapper
public interface AccountGroupInfoMapper extends BaseMapper<AccountGroupInfo> {

	@Delete("DELETE FROM tbl_platform_account_group_info WHERE group_id = #{groupId} AND account_id = #{accountId}")
	int deleteByGroupIdAndAccountId(@Param("groupId") String groupId, @Param("accountId") String accountId);

	@Delete("DELETE FROM tbl_platform_account_group_info WHERE account_id = #{accountId}")
	int deleteByAccountId(@Param("accountId") String accountId);

}
