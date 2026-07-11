package com.phoenix.agent.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.agent.model.UserAgentInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserAgentInfoMapper extends BaseMapper<UserAgentInfo> {

    @Select("SELECT * FROM tbl_agent_user_agent_info WHERE user_id = #{userId}")
    List<UserAgentInfo> selectByUserId(String userId);

    @Select("SELECT * FROM tbl_agent_user_agent_info WHERE user_id = #{userId} AND agent_sn = #{agentSn}")
    UserAgentInfo selectByUserIdAndAgentSn(@Param("userId") String userId, @Param("agentSn") String agentSn);

}
