package com.phoenix.data.service.datasource;

import com.mybatisflex.core.service.IService;
import com.phoenix.data.bo.DbConfigBO;
import com.phoenix.data.entity.AgentDatasource;
import com.phoenix.data.entity.Datasource;
import com.phoenix.data.entity.LogicalRelation;
import java.util.List;

/**
 * 数据源服务接口
 */
public interface DatasourceService extends IService<Datasource> {

	/**
	 * 获取所有数据源列表
	 * @return 数据源列表
	 */
	List<Datasource> getAllDatasource();

	/**
	 * 根据状态获取数据源列表
	 * @param status 数据源状态
	 * @return 数据源列表
	 */
	List<Datasource> getDatasourceByStatus(String status);

	/**
	 * 根据类型获取数据源列表
	 * @param type 数据源类型
	 * @return 数据源列表
	 */
	List<Datasource> getDatasourceByType(String type);

	/**
	 * 根据ID获取数据源详情
	 * @param id 数据源ID
	 * @return 数据源
	 */
	Datasource getDatasourceById(Integer id);

	/**
	 * 创建数据源
	 * @param datasource 数据源实体
	 * @return 创建后的数据源
	 */
	Datasource createDatasource(Datasource datasource);

	/**
	 * 更新数据源
	 * @param id 数据源ID
	 * @param datasource 数据源实体
	 * @return 更新后的数据源
	 */
	Datasource updateDatasource(Integer id, Datasource datasource);

	/**
	 * 删除数据源
	 * @param id 数据源ID
	 */
	void deleteDatasource(Integer id);

	/**
	 * 更新数据源测试连接状态
	 * @param id 数据源ID
	 * @param testStatus 测试状态
	 */
	void updateTestStatus(Integer id, String testStatus);

	/**
	 * 测试数据源连接
	 * @param id 数据源ID
	 * @return 连接是否成功
	 */
	boolean testConnection(Integer id);

	/**
	 * 获取智能体关联的数据源列表
	 * @deprecated 应使用 AgentDatasourceService 中的方法
	 * @param agentId 智能体ID
	 * @return 数据源关联列表
	 */
	@Deprecated
	List<AgentDatasource> getAgentDatasource(Long agentId);

	/**
	 * 获取数据源的表名列表
	 * @param datasourceId 数据源ID
	 * @return 表名列表
	 * @throws Exception 获取异常
	 */
	List<String> getDatasourceTables(Integer datasourceId) throws Exception;

	/**
	 * 获取数据源表的字段列表
	 */
	List<String> getTableColumns(Integer datasourceId, String tableName) throws Exception;

	/**
	 * 获取数据源的数据库配置
	 * @param datasource 数据源
	 * @return 数据库配置
	 */
	DbConfigBO getDbConfig(Datasource datasource);

	/**
	 * 获取数据源的逻辑外键列表
	 */
	List<LogicalRelation> getLogicalRelations(Integer datasourceId);

	/**
	 * 添加逻辑外键
	 */
	LogicalRelation addLogicalRelation(Integer datasourceId, LogicalRelation logicalRelation);

	/**
	 * 更新逻辑外键
	 */
	LogicalRelation updateLogicalRelation(Integer datasourceId, Integer relationId, LogicalRelation logicalRelation);

	/**
	 * 删除逻辑外键
	 */
	void deleteLogicalRelation(Integer datasourceId, Integer logicalRelationId);

	/**
	 * 批量保存逻辑外键（替换现有的所有外键）
	 */
	List<LogicalRelation> saveLogicalRelations(Integer datasourceId, List<LogicalRelation> logicalRelations);

}
