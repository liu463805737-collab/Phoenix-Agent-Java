package com.phoenix.platform.service.front.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.platform.mapper.front.AccountGroupInfoMapper;
import com.phoenix.platform.model.front.AccountGroupInfo;
import com.phoenix.platform.model.front.GroupInfo;
import com.phoenix.platform.service.front.AccountGroupInfoService;
import com.phoenix.common.vo.front.UserGroupVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class AccountGroupInfoServiceImpl extends ServiceImpl<AccountGroupInfoMapper, AccountGroupInfo>
		implements AccountGroupInfoService {

	@Override
	public Page<AccountGroupInfo> page(Page<AccountGroupInfo> page, AccountGroupInfo query) {
		return QueryChain.of(getMapper())
			.eq(AccountGroupInfo::getGroupId, query.getGroupId(), StrUtil.isNotBlank(query.getGroupId()))
			.eq(AccountGroupInfo::getAccountId, query.getAccountId(), StrUtil.isNotBlank(query.getAccountId()))
			.orderBy(AccountGroupInfo::getCreateTime, false)
			.page(page);
	}

	@Override
	public List<AccountGroupInfo> getByGroupId(String groupId) {
		return QueryChain.of(getMapper())
			.eq(AccountGroupInfo::getGroupId, groupId)
			.orderBy(AccountGroupInfo::getCreateTime, false)
			.list();
	}

	@Override
	public List<AccountGroupInfo> getByAccountId(String accountId) {
		return QueryChain.of(getMapper())
			.eq(AccountGroupInfo::getAccountId, accountId)
			.orderBy(AccountGroupInfo::getCreateTime, false)
			.list();
	}

	@Override
	public List<UserGroupVO> getUserGroupsByAccountId(String accountId) {
		QueryWrapper qw = QueryWrapper.create()
			.select(AccountGroupInfo::getGroupId)
			.select(AccountGroupInfo::getGroupName)
			.select(GroupInfo::getDescription)
			.from(AccountGroupInfo.class)
			.leftJoin(GroupInfo.class).on(AccountGroupInfo::getGroupId, GroupInfo::getId)
			.where(AccountGroupInfo::getAccountId).eq(accountId);
		return getMapper().selectListByQueryAs(qw, UserGroupVO.class);
	}

	@Override
	public boolean deleteByGroupIdAndAccountId(String groupId, String accountId) {
		return getMapper().deleteByGroupIdAndAccountId(groupId, accountId) > 0;
	}

	@Override
	public boolean deleteByAccountId(String accountId) {
		return getMapper().deleteByAccountId(accountId) > 0;
	}

}
