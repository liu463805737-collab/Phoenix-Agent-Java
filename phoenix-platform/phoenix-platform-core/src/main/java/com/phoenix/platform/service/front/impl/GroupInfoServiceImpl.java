package com.phoenix.platform.service.front.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.platform.mapper.front.GroupInfoMapper;
import com.phoenix.platform.model.front.AccountGroupInfo;
import com.phoenix.platform.model.front.GroupAgentInfo;
import com.phoenix.platform.model.front.GroupInfo;
import com.phoenix.platform.service.front.AccountGroupInfoService;
import com.phoenix.platform.service.front.GroupAgentInfoService;
import com.phoenix.platform.service.front.GroupInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 组织信息服务实现
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo> implements GroupInfoService {

	private final GroupAgentInfoService groupAgentInfoService;
	private final AccountGroupInfoService accountGroupInfoService;

	@Override
	public List<GroupInfo> getByLoginId(String loginId) {
		return QueryChain.of(getMapper())
			.leftJoin(AccountGroupInfo.class).on(GroupInfo::getId, AccountGroupInfo::getGroupId)
			.where(AccountGroupInfo::getAccountId).eq(loginId)
			.eq(GroupInfo::getStatus, 0)
			.list();
	}

	@Override
	public Page<GroupInfo> page(Page<GroupInfo> page, GroupInfo query) {
		return QueryChain.of(getMapper())
			.like(GroupInfo::getName, query.getName(), StrUtil.isNotBlank(query.getName()))
			.eq(GroupInfo::getSn, query.getSn(), StrUtil.isNotBlank(query.getSn()))
			.orderBy(GroupInfo::getCreateTime, false)
			.page(page);
	}

	@Override
	public GroupInfo getBySn(String sn) {
		return QueryChain.of(getMapper()).eq(GroupInfo::getSn, sn).one();
	}

	@Override
	public boolean deleteById(String id) {
		QueryWrapper queryWrapper = new QueryWrapper();
		queryWrapper.eq(GroupAgentInfo::getGroupId, id);
		groupAgentInfoService.remove(queryWrapper);
		queryWrapper = new QueryWrapper();
		queryWrapper.eq(AccountGroupInfo::getGroupId, id);
		accountGroupInfoService.remove(queryWrapper);
		return this.removeById(id);
	}

	@Override
	public boolean toggleStatus(String id) {
		GroupInfo group = this.getById(id);
		if (group == null) {
			return false;
		}
		group.setStatus(group.getStatus() == null || group.getStatus() == 0 ? 1 : 0);
		return this.updateById(group);
	}
}
