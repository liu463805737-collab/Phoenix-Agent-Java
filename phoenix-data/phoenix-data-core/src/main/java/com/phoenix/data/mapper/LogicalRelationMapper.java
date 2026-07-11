package com.phoenix.data.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.data.entity.LogicalRelation;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 逻辑外键配置 Mapper 接口
 */
@Mapper
public interface LogicalRelationMapper extends BaseMapper<LogicalRelation> {

	/**
	 * 根据ID查询逻辑外键
	 */
	@Select("SELECT * FROM tbl_data_logical_relation WHERE id = #{id} AND is_deleted = 0")
	LogicalRelation selectById(@Param("id") Integer id);

	/**
	 * 根据数据源ID查询逻辑外键列表（未删除的）
	 */
	@Select("SELECT * FROM tbl_data_logical_relation WHERE datasource_id = #{datasourceId} AND is_deleted = 0 ORDER BY created_time DESC")
	List<LogicalRelation> selectByDatasourceId(@Param("datasourceId") Integer datasourceId);

	/**
	 * 更新逻辑外键
	 */
	@Update("""
			<script>
			UPDATE tbl_data_logical_relation
			<set>
			    <if test="sourceTableName != null">source_table_name = #{sourceTableName},</if>
			    <if test="sourceColumnName != null">source_column_name = #{sourceColumnName},</if>
			    <if test="targetTableName != null">target_table_name = #{targetTableName},</if>
			    <if test="targetColumnName != null">target_column_name = #{targetColumnName},</if>
			    <if test="relationType != null">relation_type = #{relationType},</if>
			    <if test="description != null">description = #{description},</if>
			    updated_time = NOW()
			</set>
			WHERE id = #{id}
			</script>
			""")
	int updateById(LogicalRelation logicalRelation);

	/**
	 * 逻辑删除外键
	 */
	@Update("UPDATE tbl_data_logical_relation SET is_deleted = 1, updated_time = NOW() WHERE id = #{id}")
	int logicalDeleteById(@Param("id") Integer id);

	/**
	 * 逻辑删除数据源下的所有逻辑外键
	 */
	@Update("UPDATE tbl_data_logical_relation SET is_deleted = 1, updated_time = NOW() WHERE datasource_id = #{datasourceId}")
	int deleteByDatasourceId(@Param("datasourceId") Integer datasourceId);

	/**
	 * 检查逻辑外键是否存在
	 */
	@Select("""
			SELECT COUNT(*) FROM tbl_data_logical_relation
			WHERE datasource_id = #{datasourceId}
			  AND source_table_name = #{sourceTableName}
			  AND source_column_name = #{sourceColumnName}
			  AND target_table_name = #{targetTableName}
			  AND target_column_name = #{targetColumnName}
			  AND is_deleted = 0
			""")
	int checkExists(@Param("datasourceId") Integer datasourceId, @Param("sourceTableName") String sourceTableName,
			@Param("sourceColumnName") String sourceColumnName, @Param("targetTableName") String targetTableName,
			@Param("targetColumnName") String targetColumnName);

}
