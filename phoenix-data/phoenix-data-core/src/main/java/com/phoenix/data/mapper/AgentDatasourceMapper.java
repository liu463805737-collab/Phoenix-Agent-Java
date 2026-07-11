package com.phoenix.data.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.data.entity.AgentDatasource;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 智能体数据源关联 Mapper 接口
 */
@Mapper
public interface AgentDatasourceMapper extends BaseMapper<AgentDatasource> {

	/** Query associated data sources by agent ID (including data source details) */
	@Select("SELECT ad.*, d.name, d.type, d.host, d.port, d.database_name, "
			+ "d.connection_url, d.username, d.password, d.status, d.test_status, d.description "
			+ "FROM tbl_data_agent_datasource ad " + "LEFT JOIN tbl_data_datasource d ON ad.datasource_id = d.id "
			+ "WHERE ad.agent_id = #{agentId} " + "ORDER BY ad.create_time DESC")
	List<AgentDatasource> selectByAgentIdWithDatasource(@Param("agentId") Long agentId);

	/** Query associated data sources by agent ID */
	@Select("SELECT * FROM tbl_data_agent_datasource WHERE agent_id = #{agentId} ORDER BY create_time DESC")
	List<AgentDatasource> selectByAgentId(@Param("agentId") Long agentId);

	/** Query active datasource ID by agent ID */
	@Select("SELECT datasource_id FROM tbl_data_agent_datasource WHERE agent_id = #{agentId} AND is_active = 1")
	Integer selectActiveDatasourceIdByAgentId(@Param("agentId") Long agentId);

	/** Query association by agent ID and data source ID */
	@Select("SELECT * FROM tbl_data_agent_datasource WHERE agent_id = #{agentId} AND datasource_id = #{datasourceId}")
	AgentDatasource selectByAgentIdAndDatasourceId(@Param("agentId") Long agentId,
			@Param("datasourceId") Integer datasourceId);

	/** Disable all data sources for an agent */
	@Update("UPDATE tbl_data_agent_datasource SET is_active = 0 WHERE agent_id = #{agentId}")
	int disableAllByAgentId(@Param("agentId") Long agentId);

	/**
	 * Count the number of enabled data sources for an agent (excluding the specified data
	 * source)
	 */
	@Select("SELECT COUNT(*) FROM tbl_data_agent_datasource WHERE agent_id = #{agentId} AND is_active = 1 AND datasource_id != #{excludeDatasourceId}")
	int countActiveByAgentIdExcluding(@Param("agentId") Long agentId,
			@Param("excludeDatasourceId") Integer excludeDatasourceId);

	/**
	 * 根据数据源ID删除所有关联记录
	 */
	@Delete("DELETE FROM tbl_data_agent_datasource WHERE datasource_id = #{datasourceId}")
	int deleteAllByDatasourceId(@Param("datasourceId") Integer datasourceId);

	/**
	 * 创建新的已启用的智能体-数据源关联关系
	 */
	@Insert("INSERT INTO tbl_data_agent_datasource (agent_id, datasource_id, is_active) VALUES (#{agentId}, #{datasourceId}, 1)")
	int createNewRelationEnabled(@Param("agentId") Long agentId, @Param("datasourceId") Integer datasourceId);

	/**
	 * 更新智能体与数据源的关联状态
	 */
	@Update("UPDATE tbl_data_agent_datasource SET is_active = #{isActive} WHERE agent_id = #{agentId} AND datasource_id = #{datasourceId}")
	int updateRelation(@Param("agentId") Long agentId, @Param("datasourceId") Integer datasourceId,
			@Param("isActive") Integer isActive);

	/**
	 * 启用智能体与数据源的关联关系
	 */
	default int enableRelation(Long agentId, Integer datasourceId) {
		return updateRelation(agentId, datasourceId, 1);
	}

	/**
	 * 删除智能体与数据源的关联关系
	 */
	@Delete("DELETE FROM tbl_data_agent_datasource WHERE agent_id = #{agentId} AND datasource_id = #{datasourceId}")
	int removeRelation(@Param("agentId") Long agentId, @Param("datasourceId") Integer datasourceId);

}
