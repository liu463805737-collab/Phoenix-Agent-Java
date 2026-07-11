package com.phoenix.platform.mapper.front;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.platform.model.front.GroupAgentInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GroupAgentInfoMapper extends BaseMapper<GroupAgentInfo> {

	@Delete("DELETE FROM tbl_platform_group_agent_info WHERE group_id = #{groupId} AND agent_id = #{agentId}")
	int deleteByGroupIdAndAgentId(@Param("groupId") String groupId, @Param("agentId") String agentId);

}
