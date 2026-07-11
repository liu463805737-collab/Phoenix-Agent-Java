package com.phoenix.data.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.data.entity.AgentDatasourceTables;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 智能体数据源表选择 Mapper 接口
 */
@Mapper
public interface AgentDatasourceTablesMapper extends BaseMapper<AgentDatasourceTables> {

	/**
	 * 查询智能体数据源所有已选表名
	 */
	@Select("select table_name from tbl_data_agent_datasource_tables where agent_datasource_id = #{agentDatasourceId}")
	List<String> getAgentDatasourceTables(@Param("agentDatasourceId") int agentDatasourceId);

	/**
	 * 删除当前列表中不存在的过期表
	 */
	@Delete("<script>" + "DELETE FROM tbl_data_agent_datasource_tables WHERE agent_datasource_id = #{agentDatasourceId}"
			+ "<if test='tables != null and tables.size() > 0'>" + " AND table_name NOT IN ("
			+ "<foreach collection='tables' item='table' separator=','>#{table}</foreach>" + ")" + "</if>"
			+ "</script>")
	int removeExpireTables(@Param("agentDatasourceId") int agentDatasourceId, @Param("tables") List<String> tables);

	/**
	 * 删除智能体数据源所有表
	 */
	@Delete("DELETE FROM tbl_data_agent_datasource_tables WHERE agent_datasource_id = #{agentDatasourceId}")
	int removeAllTables(@Param("agentDatasourceId") int agentDatasourceId);

	/**
	 * 插入用户选择的表列表
	 */
	@Insert("<script>" + "INSERT INTO tbl_data_agent_datasource_tables (agent_datasource_id, table_name) VALUES "
			+ "<if test='tables != null and tables.size() > 0'>"
			+ "<foreach collection='tables' item='table' separator=','>" + "(#{agentDatasourceId}, #{table})"
			+ "</foreach>" + " ON CONFLICT DO NOTHING </if>" + "</script>")
	int insertNewTables(@Param("agentDatasourceId") int agentDatasourceId, @Param("tables") List<String> tables);

	/**
	 * 更新智能体数据源的表选择（先删除过期表，再插入新表）
	 * @param agentDatasourceId 智能体数据源ID
	 * @param tables 表名列表
	 * @throws IllegalArgumentException 如果tables为空
	 */
	default int updateAgentDatasourceTables(int agentDatasourceId, List<String> tables) {
		if (tables.isEmpty()) {
			throw new IllegalArgumentException("tables cannot be empty");
		}
		int deleteCount = removeExpireTables(agentDatasourceId, tables);
		int insertCount = insertNewTables(agentDatasourceId, tables);
		return deleteCount + insertCount;
	}

}
