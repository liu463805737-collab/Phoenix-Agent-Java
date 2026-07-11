package com.phoenix.data.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.data.entity.SemanticModel;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 语义模型 Mapper 接口
 */
@Mapper
public interface SemanticModelMapper extends BaseMapper<SemanticModel> {

	/**
	 * 根据智能体ID查询语义模型列表
	 */
	@Select("""
			SELECT * FROM tbl_data_semantic_model
			WHERE agent_id = #{agentId}
			ORDER BY created_time DESC
			""")
	List<SemanticModel> selectByAgentId(@Param("agentId") Long agentId);

	/**
	 * 根据ID查询语义模型
	 */
	@Select("""
			SELECT * FROM tbl_data_semantic_model
			WHERE id = #{id}
			""")
	SemanticModel selectById(@Param("id") Long id);

	/**
	 * 根据关键词搜索语义模型
	 */
	@Select("""
			SELECT * FROM tbl_data_semantic_model
			WHERE column_name LIKE '%' || #{keyword} || '%'
			   OR business_name LIKE '%' || #{keyword} || '%'
			   OR business_description LIKE '%' || #{keyword} || '%'
			   OR synonyms LIKE '%' || #{keyword} || '%'
			ORDER BY created_time DESC
			""")
	List<SemanticModel> searchByKeyword(@Param("keyword") String keyword);

	/**
	 * 批量启用字段
	 */
	@Update("""
			UPDATE tbl_data_semantic_model
			SET status = 1
			WHERE id = #{id}
			""")
	int enableById(@Param("id") Long id);

	/**
	 * 批量禁用字段
	 */
	@Update("""
			UPDATE tbl_data_semantic_model
			SET status = 0
			WHERE id = #{id}
			""")
	int disableById(@Param("id") Long id);

	/**
	 * 查询智能体已启用的语义模型列表
	 */
	@Select("""
			SELECT * FROM tbl_data_semantic_model
			WHERE agent_id = #{agentId}
			  AND status != 0
			ORDER BY created_time DESC
			""")
	List<SemanticModel> selectEnabledByAgentId(@Param("agentId") Long agentId);

	/**
	 * 根据ID更新语义模型（仅更新非空字段）
	 */
	@Update("""
			<script>
			UPDATE tbl_data_semantic_model
			<set>
			    <if test="agentId != null">agent_id = #{agentId},</if>
			    <if test="datasourceId != null">datasource_id = #{datasourceId},</if>
			    <if test="tableName != null">table_name = #{tableName},</if>
				<if test="columnName != null">column_name = #{columnName},</if>
				<if test="businessName != null">business_name = #{businessName},</if>
				<if test="synonyms != null">synonyms = #{synonyms},</if>
				<if test="businessDescription != null">business_description = #{businessDescription},</if>
				<if test="columnComment != null">column_comment = #{columnComment},</if>
				<if test="dataType != null">data_type = #{dataType},</if>
				<if test="status != null">status = #{status},</if>
				updated_time = NOW()
			</set>
			WHERE id = #{id}
			</script>
			""")
	int updateById(SemanticModel model);

	/**
	 * 根据数据源ID和表名列表查询语义模型
	 */
	@Select("""
			<script>
			SELECT * FROM tbl_data_semantic_model
			WHERE datasource_id = #{datasourceId}
			  AND status = 1
			  AND table_name IN
			  <foreach item='tableName' index='index' collection='tableNames' open='(' separator=',' close=')'>
			    #{tableName}
			  </foreach>
			ORDER BY created_time DESC
			</script>
			""")
	List<SemanticModel> selectByDatasourceIdAndTableNames(@Param("datasourceId") Integer datasourceId,
			@Param("tableNames") List<String> tableNames);

	/**
	 * 根据智能体ID、表名和列名查询语义模型
	 */
	@Select("""
			SELECT * FROM tbl_data_semantic_model
			WHERE agent_id = #{agentId}
			  AND table_name = #{tableName}
			  AND column_name = #{columnName}
			LIMIT 1
			""")
	SemanticModel selectByAgentIdAndTableNameAndColumnName(@Param("agentId") Integer agentId,
			@Param("tableName") String tableName, @Param("columnName") String columnName);

}
