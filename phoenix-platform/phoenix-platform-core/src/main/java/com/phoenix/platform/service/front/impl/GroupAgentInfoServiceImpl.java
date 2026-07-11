package com.phoenix.platform.service.front.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.platform.mapper.front.GroupAgentInfoMapper;
import com.phoenix.platform.model.front.GroupAgentInfo;
import com.phoenix.platform.service.front.GroupAgentInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class GroupAgentInfoServiceImpl extends ServiceImpl<GroupAgentInfoMapper, GroupAgentInfo>
        implements GroupAgentInfoService {
    @Override
    public List<GroupAgentInfo> getByGroupIds(List<String> groupIds) {
        return QueryChain.of(this.mapper)
                .in(GroupAgentInfo::getGroupId, groupIds)
                .list();
    }

    @Override
    public Page<GroupAgentInfo> page(Page<GroupAgentInfo> page, GroupAgentInfo query) {
        return QueryChain.of(getMapper())
                .eq(GroupAgentInfo::getGroupId, query.getGroupId(), StrUtil.isNotBlank(query.getGroupId()))
                .eq(GroupAgentInfo::getAgentId, query.getAgentId(), query.getAgentId() != null)
                .orderBy(GroupAgentInfo::getCreateTime, false)
                .page(page);
    }

    @Override
    public List<GroupAgentInfo> getByGroupId(String groupId) {
        return QueryChain.of(getMapper())
                .eq(GroupAgentInfo::getGroupId, groupId)
                .orderBy(GroupAgentInfo::getCreateTime, false)
                .list();
    }

    @Override
    public List<GroupAgentInfo> getByAgentId(String agentId) {
        return QueryChain.of(getMapper())
                .eq(GroupAgentInfo::getAgentId, agentId)
                .orderBy(GroupAgentInfo::getCreateTime, false)
                .list();
    }

    @Override
    public boolean deleteByGroupIdAndAgentId(String groupId, String agentId) {
        return getMapper().deleteByGroupIdAndAgentId(groupId, agentId) > 0;
    }
}
