package com.phoenix.data.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.data.entity.BusinessKnowledge;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 业务知识 Mapper 接口
 */
@Mapper
public interface BusinessKnowledgeMapper extends BaseMapper<BusinessKnowledge> {

	/**
	 * 根据智能体ID查询业务知识列表
	 */
	@Select("""
			SELECT * FROM tbl_data_business_knowledge
			WHERE agent_id = #{agentId} AND is_deleted = 0
			ORDER BY created_time DESC
			""")
	List<BusinessKnowledge> selectByAgentId(@Param("agentId") Long agentId);

	/**
	 * 根据关键词在指定智能体范围内搜索业务知识
	 */
	@Select("""
			SELECT * FROM tbl_data_business_knowledge
			WHERE agent_id = #{agentId} AND is_deleted = 0
			  AND (business_term LIKE '%' || #{keyword} || '%'
			    OR description LIKE '%' || #{keyword} || '%'
			    OR synonyms LIKE '%' || #{keyword} || '%')
			ORDER BY created_time DESC
			""")
	List<BusinessKnowledge> searchInAgent(@Param("agentId") Long agentId, @Param("keyword") String keyword);

	/**
	 * 根据ID更新业务知识
	 */
	@Update("""
			<script>
			UPDATE tbl_data_business_knowledge
			<set>
				<if test="businessTerm != null">business_term = #{businessTerm},</if>
				<if test="description != null">description = #{description},</if>
				<if test="synonyms != null">synonyms = #{synonyms},</if>
				<if test="isRecall != null">is_recall = #{isRecall},</if>
				<if test="agentId != null">agent_id = #{agentId},</if>
				<if test="embeddingStatus != null">embedding_status = #{embeddingStatus},</if>
				<if test="errorMsg != null">error_msg = #{errorMsg},</if>
				<if test="isDeleted != null">is_deleted = #{isDeleted},</if>
				updated_time = NOW()
			</set>
			WHERE id = #{id}
			</script>
			""")
	int updateById(BusinessKnowledge knowledge);

	/**
	 * 根据ID查询业务知识
	 */
	@Select("""
			SELECT * FROM tbl_data_business_knowledge
			WHERE id = #{id} AND is_deleted = 0
			""")
	BusinessKnowledge selectById(Long id);

	/**
	 * 查询智能体已启用的召回业务知识ID列表
	 */
	@Select("""
			SELECT id FROM tbl_data_business_knowledge
			WHERE agent_id = #{agentId} AND is_recall = 1 AND is_deleted = 0
			""")
	List<Long> selectRecalledKnowledgeIds(@Param("agentId") Long agentId);

	/**
	 * 逻辑删除/恢复业务知识
	 */
	@Update("""
			UPDATE tbl_data_business_knowledge
			SET is_deleted = #{isDeleted}, updated_time = NOW()
			WHERE id = #{id}
			""")
	int logicalDelete(@Param("id") Long id, @Param("isDeleted") Integer isDeleted);

}
