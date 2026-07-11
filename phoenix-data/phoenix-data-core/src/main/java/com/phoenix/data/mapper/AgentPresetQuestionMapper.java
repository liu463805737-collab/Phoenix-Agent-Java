package com.phoenix.data.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.data.entity.AgentPresetQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 智能体预设问题 Mapper 接口
 */
@Mapper
public interface AgentPresetQuestionMapper extends BaseMapper<AgentPresetQuestion> {

	/**
	 * 根据智能体ID查询已启用的预设问题
	 */
	@Select("""
			SELECT * FROM tbl_data_agent_preset_question
			         WHERE agent_id = #{agentId} AND is_active = 1
			ORDER BY sort_order ASC, id ASC
			""")
	List<AgentPresetQuestion> selectByAgentId(@Param("agentId") Long agentId);

	/**
	 * 根据智能体ID查询所有预设问题
	 */
	@Select("""
			SELECT * FROM tbl_data_agent_preset_question
			         WHERE agent_id = #{agentId}
			ORDER BY sort_order ASC, id ASC
			""")
	List<AgentPresetQuestion> selectAllByAgentId(@Param("agentId") Long agentId);

	/**
	 * 根据ID查询预设问题
	 */
	@Select("""
			SELECT * FROM tbl_data_agent_preset_question WHERE id = #{id}
			""")
	AgentPresetQuestion selectById(@Param("id") Long id);

	/**
	 * 更新预设问题
	 */
	@Update("""
			<script>
			UPDATE tbl_data_agent_preset_question
			<set>
				<if test="question != null">question = #{question},</if>
				<if test="sortOrder != null">sort_order = #{sortOrder},</if>
				<if test="isActive != null">is_active = #{isActive},</if>
				update_time = NOW()
			</set>
			WHERE id = #{id}
			</script>
			""")
	int updateWithNow(AgentPresetQuestion question);

	/**
	 * 根据智能体ID删除所有预设问题
	 */
	@Delete("""
			DELETE FROM tbl_data_agent_preset_question WHERE agent_id = #{agentId}
			""")
	int deleteByAgentId(@Param("agentId") Long agentId);

}
