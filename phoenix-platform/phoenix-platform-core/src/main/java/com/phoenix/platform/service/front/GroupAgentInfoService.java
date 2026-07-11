package com.phoenix.platform.service.front;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.phoenix.platform.model.front.GroupAgentInfo;

import java.util.List;

public interface GroupAgentInfoService extends IService<GroupAgentInfo> {

    Page<GroupAgentInfo> page(Page<GroupAgentInfo> page, GroupAgentInfo query);

    List<GroupAgentInfo> getByGroupId(String groupId);

    List<GroupAgentInfo> getByAgentId(String agentId);

    boolean deleteByGroupIdAndAgentId(String groupId, String agentId);

    List<GroupAgentInfo> getByGroupIds(List<String> groupIds);
}
