package com.phoenix.data.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.data.dto.knowledge.agentknowledge.AgentKnowledgeQueryDTO;
import com.phoenix.data.entity.AgentKnowledge;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能体知识库 Mapper 接口
 */
@Mapper
public interface AgentKnowledgeMapper extends BaseMapper<AgentKnowledge> {

	/**
	 * 根据ID查询知识（未删除的）
	 */
	@Select("""
			SELECT * FROM tbl_data_agent_knowledge WHERE id = #{id} AND is_deleted = 0
			""")
	AgentKnowledge selectById(@Param("id") Integer id);

	/**
	 * 根据ID查询知识（包含已删除的）
	 */
	@Select("""
			    SELECT * FROM tbl_data_agent_knowledge WHERE id = #{id}
			""")
	AgentKnowledge selectByIdIncludeDeleted(@Param("id") Integer id);

	/**
	 * 更新知识记录
	 */
	@Update("""
			<script>
			UPDATE tbl_data_agent_knowledge
			<set>
				<if test="title != null">title = #{title},</if>
				<if test="content != null">content = #{content},</if>
				<if test="type != null">type = #{type},</if>
				<if test="question != null">question = #{question},</if>
				<if test="isRecall != null">is_recall = #{isRecall},</if>
				<if test="embeddingStatus != null">embedding_status = #{embeddingStatus},</if>
				<if test="errorMsg != null">error_msg = #{errorMsg},</if>
				<if test="sourceFilename != null">source_filename = #{sourceFilename},</if>
				<if test="filePath != null">file_path = #{filePath},</if>
				<if test="fileSize != null">file_size = #{fileSize},</if>
				<if test="fileType != null">file_type = #{fileType},</if>
				<if test="splitterType != null">splitter_type = #{splitterType},</if>
				<if test="isDeleted != null">is_deleted = #{isDeleted},</if>
				<if test="isResourceCleaned != null">is_resource_cleaned = #{isResourceCleaned},</if>
				updated_time = NOW()
			</set>
			WHERE id = #{id}
			</script>
			""")
	int updateWithNow(AgentKnowledge knowledge);

	/**
	 * 根据条件分页查询知识列表
	 */
	@Select("""
			<script>
			SELECT * FROM tbl_data_agent_knowledge
			WHERE agent_id = #{queryDTO.agentId}
			<if test="queryDTO.title != null and queryDTO.title != ''">
				AND title LIKE '%' || #{queryDTO.title} || '%'
			</if>
			<if test="queryDTO.type != null and queryDTO.type != ''">
				AND type = #{queryDTO.type}
			</if>
			<if test="queryDTO.embeddingStatus != null and queryDTO.embeddingStatus != ''">
				AND embedding_status = #{queryDTO.embeddingStatus}
			</if>
			AND is_deleted = 0
			LIMIT #{queryDTO.pageSize} OFFSET #{offset}
			</script>
			""")
	List<AgentKnowledge> selectByConditionsWithPage(@Param("queryDTO") AgentKnowledgeQueryDTO queryDTO,
			@Param("offset") Integer offset);

	/**
	 * 根据条件统计知识数量
	 */
	@Select("""
			<script>
			SELECT COUNT(*) FROM tbl_data_agent_knowledge
			WHERE agent_id = #{queryDTO.agentId}
			<if test="queryDTO.title != null and queryDTO.title != ''">
				AND title LIKE '%' || #{queryDTO.title} || '%'
			</if>
			<if test="queryDTO.type != null and queryDTO.type != ''">
				AND type = #{queryDTO.type}
			</if>
			<if test="queryDTO.embeddingStatus != null and queryDTO.embeddingStatus != ''">
				AND embedding_status = #{queryDTO.embeddingStatus}
			</if>
			AND is_deleted = 0
			</script>
			""")
	Long countByConditions(@Param("queryDTO") AgentKnowledgeQueryDTO queryDTO);

	/**
	 * 查询智能体所有已启用的召回知识ID列表
	 */
	@Select("""
			SELECT id FROM tbl_data_agent_knowledge WHERE agent_id = #{agentId} AND is_recall = 1 AND is_deleted = 0
			""")
	List<Integer> selectRecalledKnowledgeIds(@Param("agentId") Integer agentId);

	/**
	 * 查询待清理的“僵尸”记录 条件：is_deleted = 1 AND is_resource_cleaned = 0 AND updated_time <(当前时间
	 * - N分钟)
	 */
	@Select("""
			    SELECT * FROM tbl_data_agent_knowledge
			    WHERE is_deleted = 1
			      AND is_resource_cleaned = 0
			      AND updated_time < #{beforeTime}
			    LIMIT #{limit}
			""")
	List<AgentKnowledge> selectDirtyRecords(@Param("beforeTime") LocalDateTime beforeTime, @Param("limit") int limit);

}
