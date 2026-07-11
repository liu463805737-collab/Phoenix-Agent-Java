package com.phoenix.data.service.datasource.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.data.bo.DbConfigBO;
import com.phoenix.data.dto.datasource.SchemaInitRequest;
import com.phoenix.data.entity.AgentDatasource;
import com.phoenix.data.entity.Datasource;
import com.phoenix.data.mapper.AgentDatasourceMapper;
import com.phoenix.data.mapper.AgentDatasourceTablesMapper;
import com.phoenix.data.service.datasource.AgentDatasourceService;
import com.phoenix.data.service.datasource.DatasourceService;
import com.phoenix.data.service.schema.SchemaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 * 智能体数据源关联服务实现类
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AgentDatasourceServiceImpl extends ServiceImpl<AgentDatasourceMapper, AgentDatasource> implements AgentDatasourceService {

	private final DatasourceService datasourceService;

	private final SchemaService schemaService;

	private final AgentDatasourceTablesMapper tablesMapper;

	public AgentDatasourceServiceImpl(DatasourceService datasourceService, SchemaService schemaService, AgentDatasourceTablesMapper tablesMapper) {
		this.datasourceService = datasourceService;
		this.schemaService = schemaService;
		this.tablesMapper = tablesMapper;
	}

	/**
	 * 为智能体初始化指定数据源的Schema结构
	 * @param agentId 智能体ID
	 * @param datasourceId 数据源ID
	 * @param tables 需要初始化的表名列表
	 * @return 是否初始化成功
	 */
	@Override
	public Boolean initializeSchemaForAgentWithDatasource(Long agentId, Integer datasourceId, List<String> tables) {
		Assert.notNull(agentId, "Agent ID cannot be null");
		Assert.notNull(datasourceId, "Datasource ID cannot be null");
		Assert.notEmpty(tables, "Tables cannot be empty");
		try {
			String agentIdStr = String.valueOf(agentId);
			log.info("Initializing schema for agent: {} with datasource: {}, tables: {}", agentIdStr, datasourceId,
					tables);

			// Get data source information
			Datasource datasource = datasourceService.getDatasourceById(datasourceId);
			if (datasource == null) {
				throw new RuntimeException("Datasource not found with id: " + datasourceId);
			}

			// Create database configuration
			DbConfigBO dbConfig = datasourceService.getDbConfig(datasource);

			// Create SchemaInitRequest
			SchemaInitRequest schemaInitRequest = new SchemaInitRequest();
			schemaInitRequest.setDbConfig(dbConfig);
			schemaInitRequest.setTables(tables);

			log.info("Created SchemaInitRequest for agent: {}, dbConfig: {}, tables: {}", agentIdStr, dbConfig, tables);
			// Call the original initialization method
			return schemaService.schema(datasourceId, agentId, schemaInitRequest);

		}
		catch (Exception e) {
			log.error("Failed to initialize schema for agent: {} with datasource: {}", agentId, datasourceId, e);
			throw new RuntimeException("Failed to initialize schema for agent " + agentId + ": " + e.getMessage(), e);
		}
	}

	/**
	 * 获取指定智能体的数据源关联列表
	 * @param agentId 智能体ID
	 * @return 数据源关联列表
	 */
	@Override
	@Transactional(readOnly = true)
	public List<AgentDatasource> getAgentDatasource(Long agentId) {
		Assert.notNull(agentId, "Agent ID cannot be null");
		List<AgentDatasource> adentDatasources = getMapper().selectByAgentIdWithDatasource(agentId);

		// Manually fill in the data source information (since MyBatis Plus does not
		// directly support complex join query result mapping)
		for (AgentDatasource agentDatasource : adentDatasources) {
			if (agentDatasource.getDatasourceId() != null) {
				Datasource datasource = datasourceService.getDatasourceById(agentDatasource.getDatasourceId());
				agentDatasource.setDatasource(datasource);
			}
			// 获取选中的数据表
			int id = agentDatasource.getId();
			List<String> tables = tablesMapper.getAgentDatasourceTables(id);
			agentDatasource.setSelectTables(Optional.ofNullable(tables).orElse(List.of()));
		}

		return adentDatasources;
	}

	/**
	 * 为智能体添加数据源关联
	 * @param agentId 智能体ID
	 * @param datasourceId 数据源ID
	 * @return 创建的数据源关联
	 */
	@Override
	public AgentDatasource addDatasourceToAgent(Long agentId, Integer datasourceId) {
		// First, disable other data sources for this agent (an agent can only have one
		// enabled data source)
		getMapper().disableAllByAgentId(agentId);

		// Check if an association already exists
		AgentDatasource existing = getMapper().selectByAgentIdAndDatasourceId(agentId, datasourceId);

		AgentDatasource result;
		if (existing != null) {
			// If it exists, activate the association
			getMapper().enableRelation(agentId, datasourceId);

			// 删除已有的表
			tablesMapper.removeAllTables(existing.getId());

			// Query and return the updated association
			result = getMapper().selectByAgentIdAndDatasourceId(agentId, datasourceId);
		}
		else {
			// If it does not exist, create a new association
			AgentDatasource agentDatasource = new AgentDatasource(agentId, datasourceId);
			agentDatasource.setIsActive(1);
			getMapper().createNewRelationEnabled(agentId, datasourceId);
			result = agentDatasource;
		}
		result.setSelectTables(List.of());
		return result;
	}

	/**
	 * 移除智能体的数据源关联
	 * @param agentId 智能体ID
	 * @param datasourceId 数据源ID
	 */
	@Override
	public void removeDatasourceFromAgent(Long agentId, Integer datasourceId) {
		getMapper().removeRelation(agentId, datasourceId);
	}

	/**
	 * 切换智能体数据源的启用状态
	 * @param agentId 智能体ID
	 * @param datasourceId 数据源ID
	 * @param isActive 是否激活
	 * @return 更新后的数据源关联
	 */
	@Override
	public AgentDatasource toggleDatasourceForAgent(Long agentId, Integer datasourceId, Boolean isActive) {
		// If enabling data source, first check if there are other enabled data sources
		if (isActive) {
			int activeCount = getMapper().countActiveByAgentIdExcluding(agentId, datasourceId);
			if (activeCount > 0) {
				throw new RuntimeException("同一智能体下只能启用一个数据源，请先禁用其他数据源后再启用此数据源");
			}
		}

		// Update data source status
		int updated = getMapper().updateRelation(agentId, datasourceId, isActive ? 1 : 0);

		if (updated == 0) {
			throw new RuntimeException("未找到相关的数据源关联记录");
		}

		// Return the updated association record
		return getMapper().selectByAgentIdAndDatasourceId(agentId, datasourceId);
	}

	/**
	 * 更新智能体数据源选中的表
	 * @param agentId 智能体ID
	 * @param datasourceId 数据源ID
	 * @param tables 选中的表名列表
	 */
	@Override
	public void updateDatasourceTables(Long agentId, Integer datasourceId, List<String> tables) {
		if (agentId == null || datasourceId == null || tables == null) {
			throw new IllegalArgumentException("参数不能为空");
		}
		AgentDatasource datasource = getMapper().selectByAgentIdAndDatasourceId(agentId, datasourceId);
		if (datasource == null) {
			throw new IllegalArgumentException("未找到对应的数据源关联记录");
		}
		if (tables.isEmpty()) {
			tablesMapper.removeAllTables(datasource.getId());
		}
		else {
			tablesMapper.updateAgentDatasourceTables(datasource.getId(), tables);
		}
	}

}
