package com.phoenix.agent.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.agent.model.UserMemoryInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMemoryInfoMapper extends BaseMapper<UserMemoryInfo> {

    @Select("SELECT * FROM tbl_agent_user_memory_info WHERE user_id = #{userId}")
    List<UserMemoryInfo> selectByUserId(String userId);

    @Select("SELECT * FROM tbl_agent_user_memory_info WHERE user_id = #{userId} AND agent_sn = #{agentSn}")
    List<UserMemoryInfo> selectByUserIdAndAgentSn(@Param("userId") String userId, @Param("agentSn") String agentSn);

    @Select("SELECT * FROM tbl_agent_user_memory_info WHERE user_id = #{userId} AND agent_sn = #{agentSn} AND memory_type = #{memoryType}")
    List<UserMemoryInfo> selectByUserIdAndAgentSnAndType(@Param("userId") String userId, @Param("agentSn") String agentSn, @Param("memoryType") String memoryType);

}
