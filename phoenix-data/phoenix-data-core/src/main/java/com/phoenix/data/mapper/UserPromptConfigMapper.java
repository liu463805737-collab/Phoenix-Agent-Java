package com.phoenix.data.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.data.entity.UserPromptConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户提示词配置 Mapper 接口
 */
@Mapper
public interface UserPromptConfigMapper extends BaseMapper<UserPromptConfig> {

	/**
	 * 根据提示词类型查询配置列表
	 */
	@Select("""
			<script>
			SELECT * FROM tbl_data_user_prompt_config
			WHERE prompt_type = #{promptType}
			<if test='agentId != null'> AND agent_id = #{agentId}</if>
			ORDER BY update_time DESC
			</script>
			""")
	List<UserPromptConfig> selectByPromptType(@Param("promptType") String promptType, @Param("agentId") Long agentId);

	/**
	 * 查询指定类型已启用的配置
	 */
	@Select("""
			<script>
			SELECT * FROM tbl_data_user_prompt_config
			WHERE prompt_type = #{promptType}
			  AND enabled = 1
			<if test='agentId != null'> AND agent_id = #{agentId}</if>
			LIMIT 1
			</script>
			""")
	UserPromptConfig selectActiveByPromptType(@Param("promptType") String promptType, @Param("agentId") Long agentId);

	/**
	 * 禁用指定类型的所有配置
	 */
	@Update("""
			<script>
			UPDATE tbl_data_user_prompt_config
			SET enabled = 0
			WHERE prompt_type = #{promptType}
			<if test='agentId != null'> AND agent_id = #{agentId}</if>
			</script>
			""")
	int disableAllByPromptType(@Param("promptType") String promptType, @Param("agentId") Long agentId);

	/**
	 * 启用指定配置
	 */
	@Update("UPDATE tbl_data_user_prompt_config SET enabled = true WHERE id = #{id}")
	int enableById(@Param("id") String id);

	/**
	 * 禁用指定配置
	 */
	@Update("UPDATE tbl_data_user_prompt_config SET enabled = false WHERE id = #{id}")
	int disableById(@Param("id") String id);

	/**
	 * 根据ID查询配置
	 */
	@Select("SELECT * FROM tbl_data_user_prompt_config WHERE id = #{id}")
	UserPromptConfig selectById(String id);

	/**
	 * 根据ID更新配置（仅更新非空字段）
	 */
	@Update("""
			<script>
			UPDATE tbl_data_user_prompt_config
			<set>
			  <if test='name != null'>name = #{name},</if>
			  <if test='promptType != null'>prompt_type = #{promptType},</if>
			  <if test='agentId != null'>agent_id = #{agentId},</if>
			  <if test='systemPrompt != null'>system_prompt = #{systemPrompt},</if>
			  <if test='enabled != null'>enabled = #{enabled},</if>
			  <if test='description != null'>description = #{description},</if>
			  <if test='priority != null'>priority = #{priority},</if>
			  <if test='displayOrder != null'>display_order = #{displayOrder},</if>
			  update_time = NOW()
			</set>
			WHERE id = #{id}
			</script>
			""")
	int updateById(UserPromptConfig config);

	@Select("""
			<script>
			SELECT * FROM tbl_data_user_prompt_config
			WHERE prompt_type = #{promptType}
			  AND enabled = true
			<if test='agentId != null'> AND agent_id = #{agentId}</if>
			ORDER BY priority DESC, display_order, update_time DESC
			</script>
			""")
	List<UserPromptConfig> getActiveConfigsByType(@Param("promptType") String promptType,
			@Param("agentId") Long agentId);

	@Select("""
			<script>
			SELECT * FROM tbl_data_user_prompt_config
			WHERE prompt_type = #{promptType}
			<if test='agentId != null'> AND agent_id = #{agentId}</if>
			ORDER BY priority DESC, display_order, update_time DESC
			</script>
			""")
	List<UserPromptConfig> getConfigsByType(@Param("promptType") String promptType, @Param("agentId") Long agentId);

}
