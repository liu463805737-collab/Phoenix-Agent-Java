package com.phoenix.data.service.datasource;

import com.mybatisflex.core.service.IService;
import com.phoenix.data.entity.AgentDatasource;
import java.util.List;

/**
 * 智能体数据源关联服务接口
 */
public interface AgentDatasourceService extends IService<AgentDatasource> {

	/**
	 * 为指定智能体初始化数据源的表结构
	 * @param agentId 智能体ID
	 * @param datasourceId 数据源ID
	 * @param tables 需要初始化的表名列表
	 * @return 是否初始化成功
	 */
	Boolean initializeSchemaForAgentWithDatasource(Long agentId, Integer datasourceId, List<String> tables);

	/**
	 * 获取指定智能体的数据源列表
	 * @param agentId 智能体ID
	 * @return 数据源关联列表
	 */
	List<AgentDatasource> getAgentDatasource(Long agentId);

	/**
	 * 获取指定智能体的当前激活数据源
	 * @param agentId 智能体ID
	 * @return 当前激活的数据源
	 * @throws IllegalStateException 如果没有激活的数据源
	 */
	default AgentDatasource getCurrentAgentDatasource(Long agentId) {
		return getAgentDatasource(agentId).stream()
			.filter(a -> a.getIsActive() != 0)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("Agent " + agentId + " has no active datasource"));
	}

	/**
	 * 为智能体添加数据源
	 * @param agentId 智能体ID
	 * @param datasourceId 数据源ID
	 * @return 创建的数据源关联
	 */
	AgentDatasource addDatasourceToAgent(Long agentId, Integer datasourceId);

	/**
	 * 移除智能体的数据源关联
	 * @param agentId 智能体ID
	 * @param datasourceId 数据源ID
	 */
	void removeDatasourceFromAgent(Long agentId, Integer datasourceId);

	/**
	 * 切换智能体数据源的启用状态
	 * @param agentId 智能体ID
	 * @param datasourceId 数据源ID
	 * @param isActive 是否激活
	 * @return 更新后的数据源关联
	 */
	AgentDatasource toggleDatasourceForAgent(Long agentId, Integer datasourceId, Boolean isActive);

	/**
	 * 更新智能体数据源的选中的表
	 * @param agentId 智能体ID
	 * @param datasourceId 数据源ID
	 * @param tables 选中的表名列表
	 */
	void updateDatasourceTables(Long agentId, Integer datasourceId, List<String> tables);

}
