package com.phoenix.agent.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.agent.model.UserProfileInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserProfileInfoMapper extends BaseMapper<UserProfileInfo> {

    @Select("SELECT * FROM tbl_agent_user_profile_info WHERE user_id = #{userId}")
    UserProfileInfo selectByUserId(String userId);

    @Select("SELECT * FROM tbl_agent_user_profile_info WHERE user_id = #{userId} AND agent_sn = #{agentSn}")
    UserProfileInfo selectByUserIdAndAgentSn(@Param("userId") String userId, @Param("agentSn") String agentSn);

    @Select("SELECT * FROM tbl_agent_user_profile_info WHERE agent_sn = #{agentSn}")
    List<UserProfileInfo> selectByAgentSn(String agentSn);

    @Delete("DELETE FROM tbl_agent_user_profile_info WHERE user_id = #{userId} AND agent_sn = #{agentSn}")
    int deleteByUserIdAndAgentSn(@Param("userId") String userId, @Param("agentSn") String agentSn);

}
