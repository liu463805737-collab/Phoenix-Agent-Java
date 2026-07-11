package com.phoenix.platform.service.front;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.phoenix.platform.model.front.AccountGroupInfo;
import com.phoenix.common.vo.front.UserGroupVO;

import java.util.List;

/**
 * 账号-组织关联服务接口
 */
public interface AccountGroupInfoService extends IService<AccountGroupInfo> {

	Page<AccountGroupInfo> page(Page<AccountGroupInfo> page, AccountGroupInfo query);

	List<AccountGroupInfo> getByGroupId(String groupId);

	List<AccountGroupInfo> getByAccountId(String accountId);

	List<UserGroupVO> getUserGroupsByAccountId(String accountId);

	boolean deleteByGroupIdAndAccountId(String groupId, String accountId);

	boolean deleteByAccountId(String accountId);

}
