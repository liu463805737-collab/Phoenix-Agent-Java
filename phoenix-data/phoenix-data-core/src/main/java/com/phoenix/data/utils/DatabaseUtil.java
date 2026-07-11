package com.phoenix.data.utils;

import com.phoenix.data.bo.DbConfigBO;
import com.phoenix.data.connector.accessor.Accessor;
import com.phoenix.data.connector.accessor.AccessorFactory;
import com.phoenix.data.entity.AgentDatasource;
import com.phoenix.data.service.datasource.AgentDatasourceService;
import com.phoenix.data.service.datasource.DatasourceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility class for processing database.
 */
@Slf4j
@Component
@AllArgsConstructor
public class DatabaseUtil {

	private final AccessorFactory accessorFactory;

	private final AgentDatasourceService agentDatasourceService;

	private final DatasourceService datasourceService;

	/**
	 * 获取指定Agent的数据源配置
	 * @param agentId Agent ID
	 * @return 数据库配置
	 */
	public DbConfigBO getAgentDbConfig(Long agentId) {
		log.info("Getting datasource config for agent: {}", agentId);

		// Get the enabled data source for the agent
		AgentDatasource activeDatasource = agentDatasourceService.getCurrentAgentDatasource(agentId);
		// Convert to DbConfig
		DbConfigBO dbConfig = datasourceService.getDbConfig(activeDatasource.getDatasource());
		log.info("Successfully created DbConfig for agent {}: url={}, schema={}, type={}", agentId, dbConfig.getUrl(),
				dbConfig.getSchema(), dbConfig.getDialectType());

		return dbConfig;
	}

	/**
	 * 获取指定Agent的数据访问器
	 * @param agentId Agent ID
	 * @return 数据访问器
	 */
	public Accessor getAgentAccessor(Long agentId) {
		DbConfigBO dbConfig = getAgentDbConfig(agentId);
		return accessorFactory.getAccessorByDbConfig(dbConfig);
	}

}
