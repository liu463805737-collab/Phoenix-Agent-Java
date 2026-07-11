package com.phoenix.data.service.datasource.impl;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.data.bo.DbConfigBO;
import com.phoenix.data.bo.schema.ColumnInfoBO;
import com.phoenix.data.bo.schema.TableInfoBO;
import com.phoenix.data.connector.DbQueryParameter;
import com.phoenix.data.connector.accessor.Accessor;
import com.phoenix.data.connector.accessor.AccessorFactory;
import com.phoenix.data.connector.pool.DBConnectionPool;
import com.phoenix.data.connector.pool.DBConnectionPoolFactory;
import com.phoenix.data.entity.AgentDatasource;
import com.phoenix.data.entity.Datasource;
import com.phoenix.data.entity.LogicalRelation;
import com.phoenix.data.enums.ErrorCodeEnum;
import com.phoenix.data.mapper.AgentDatasourceMapper;
import com.phoenix.data.mapper.DatasourceMapper;
import com.phoenix.data.mapper.LogicalRelationMapper;
import com.phoenix.data.service.datasource.DatasourceService;
import com.phoenix.data.service.datasource.handler.DatasourceTypeHandler;
import com.phoenix.data.service.datasource.handler.registry.DatasourceTypeHandlerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

// todo: 检查Mapper的返回值，判断是否执行成功（或者对Mapper进行AOP）
/**
 * 数据源服务实现类
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DatasourceServiceImpl extends ServiceImpl<DatasourceMapper, Datasource> implements DatasourceService {

	private final AgentDatasourceMapper agentDatasourceMapper;

	private final LogicalRelationMapper logicalRelationMapper;

	private final DBConnectionPoolFactory poolFactory;

	private final AccessorFactory accessorFactory;

	private final DatasourceTypeHandlerRegistry datasourceTypeHandlerRegistry;

	public DatasourceServiceImpl(AgentDatasourceMapper agentDatasourceMapper, LogicalRelationMapper logicalRelationMapper, DBConnectionPoolFactory poolFactory, AccessorFactory accessorFactory, DatasourceTypeHandlerRegistry datasourceTypeHandlerRegistry) {
		this.agentDatasourceMapper = agentDatasourceMapper;
		this.logicalRelationMapper = logicalRelationMapper;
		this.poolFactory = poolFactory;
		this.accessorFactory = accessorFactory;
		this.datasourceTypeHandlerRegistry = datasourceTypeHandlerRegistry;
	}

	/**
	 * 获取所有数据源
	 * @return 数据源列表
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Datasource> getAllDatasource() {
		return list();
	}

	/**
	 * 根据状态获取数据源
	 * @param status 状态
	 * @return 数据源列表
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Datasource> getDatasourceByStatus(String status) {
		return QueryChain.of(getMapper())
			.eq(Datasource::getStatus, status)
			.orderBy(Datasource::getCreateTime, false)
			.list();
	}

	/**
	 * 根据类型获取数据源
	 * @param type 类型
	 * @return 数据源列表
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Datasource> getDatasourceByType(String type) {
		return QueryChain.of(getMapper())
			.eq(Datasource::getType, type)
			.orderBy(Datasource::getCreateTime, false)
			.list();
	}

	/**
	 * 根据ID获取数据源
	 * @param id 数据源ID
	 * @return 数据源
	 */
	@Override
	@Transactional(readOnly = true)
	public Datasource getDatasourceById(Integer id) {
		return getById(id);
	}

	/**
	 * 创建数据源
	 * @param datasource 数据源实体
	 * @return 创建后的数据源
	 */
	@Override
	public Datasource createDatasource(Datasource datasource) {
		// Generate connection URL
		DatasourceTypeHandler handler = datasourceTypeHandlerRegistry.getRequired(datasource.getType());
		String connectionUrl = handler.resolveConnectionUrl(datasource);
		if (StringUtils.isNotBlank(connectionUrl)) {
			datasource.setConnectionUrl(connectionUrl);
		}

		// Set default values
		if (datasource.getStatus() == null) {
			datasource.setStatus("active");
		}
		if (datasource.getTestStatus() == null) {
			datasource.setTestStatus("unknown");
		}

		if (datasource.getPassword() == null) {
			datasource.setPassword("");
		}

		if (datasource.getUsername() == null) {
			datasource.setUsername("");
		}

		save(datasource);
		return datasource;
	}

	/**
	 * 更新数据源
	 * @param id 数据源ID
	 * @param datasource 数据源实体
	 * @return 更新后的数据源
	 */
	@Override
	public Datasource updateDatasource(Integer id, Datasource datasource) {
		// Regenerate connection URL
		DatasourceTypeHandler handler = datasourceTypeHandlerRegistry.getRequired(datasource.getType());
		String connectionUrl = handler.resolveConnectionUrl(datasource);
		if (StringUtils.isNotBlank(connectionUrl)) {
			datasource.setConnectionUrl(connectionUrl);
		}
		datasource.setId(id);

		if (datasource.getPassword() == null) {
			datasource.setPassword("");
		}

		if (datasource.getUsername() == null) {
			datasource.setUsername("");
		}

		getMapper().update(datasource);
		return datasource;
	}

	/**
	 * 删除数据源（同时删除关联）
	 * @param id 数据源ID
	 */
	@Override
	public void deleteDatasource(Integer id) {
		// First, delete the associations
		agentDatasourceMapper.deleteAllByDatasourceId(id);

		// Then, delete the data source
		removeById(id);
	}

	/**
	 * 更新数据源测试连接状态
	 * @param id 数据源ID
	 * @param testStatus 测试状态
	 */
	@Override
	public void updateTestStatus(Integer id, String testStatus) {
		getMapper().updateTestStatusById(id, testStatus);
	}

	/**
	 * 测试数据源连接
	 * @param id 数据源ID
	 * @return 连接是否成功
	 */
	@Override
	public boolean testConnection(Integer id) {
		Datasource datasource = getDatasourceById(id);
		if (datasource == null) {
			return false;
		}
		try {
			// ping测试
			boolean connectionSuccess = realConnectionTest(datasource);
			log.info(datasource.getName() + " test connection result: " + connectionSuccess);
			// Update test status
			updateTestStatus(id, connectionSuccess ? "success" : "failed");

			return connectionSuccess;
		}
		catch (Exception e) {
			updateTestStatus(id, "failed");
			log.error("Error testing connection for datasource ID " + id + ": " + e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 实际的连接测试方法
	 * @param datasource 数据源
	 * @return 连接是否成功
	 */
	private boolean realConnectionTest(Datasource datasource) {
		// Convert Datasource to DbConfig
		DbConfigBO config = new DbConfigBO();
		DatasourceTypeHandler handler = datasourceTypeHandlerRegistry.getRequired(datasource.getType());
		String originalUrl = handler.resolveConnectionUrl(datasource);

		if (StringUtils.isNotBlank(originalUrl)) {
			originalUrl = handler.normalizeTestUrl(datasource, originalUrl);
		}
		config.setUrl(originalUrl);
		config.setUsername(datasource.getUsername());
		config.setPassword(datasource.getPassword());

		DBConnectionPool pool = poolFactory.getPoolByType(datasource.getType());
		if (pool == null) {
			return false;
		}

		ErrorCodeEnum result = pool.ping(config);
		return result == ErrorCodeEnum.SUCCESS;

	}

	/**
	 * 获取智能体关联的数据源列表（已废弃，建议使用AgentDatasourceService）
	 * @param agentId 智能体ID
	 * @return 数据源关联列表
	 */
	@Override
	@Deprecated
	public List<AgentDatasource> getAgentDatasource(Long agentId) {
		List<AgentDatasource> adentDatasources = agentDatasourceMapper.selectByAgentIdWithDatasource(agentId);

		// Manually fill in the data source information (since MyBatis Plus does not
		// directly support complex join query result mapping)
		for (AgentDatasource agentDatasource : adentDatasources) {
			if (agentDatasource.getDatasourceId() != null) {
				Datasource datasource = getById(agentDatasource.getDatasourceId());
				agentDatasource.setDatasource(datasource);
			}
		}

		return adentDatasources;
	}

	/**
	 * 获取数据源的表名列表
	 * @param datasourceId 数据源ID
	 * @return 表名列表
	 * @throws Exception 查询异常
	 */
	@Override
	@Transactional(readOnly = true)
	public List<String> getDatasourceTables(Integer datasourceId) throws Exception {
		log.info("Getting tables for datasource: {}", datasourceId);

		// Get data source information
		Datasource datasource = this.getDatasourceById(datasourceId);
		if (datasource == null) {
			throw new RuntimeException("Datasource not found with id: " + datasourceId);
		}

		// Create database configuration
		DbConfigBO dbConfig = getDbConfig(datasource);

		// Create query parameters
		DbQueryParameter queryParam = DbQueryParameter.from(dbConfig);

		// 提取schema名称
		DatasourceTypeHandler handler = datasourceTypeHandlerRegistry.getRequired(datasource.getType());
		String schemaName = handler.extractSchemaName(datasource);
		queryParam.setSchema(schemaName);

		// Query table list
		Accessor dbAccessor = accessorFactory.getAccessorByDbConfig(dbConfig);
		List<TableInfoBO> tableInfoList = dbAccessor.showTables(dbConfig, queryParam);

		// Extract table names
		List<String> tableNames = tableInfoList.stream()
			.map(TableInfoBO::getName)
			.filter(name -> name != null && !name.trim().isEmpty())
			.sorted()
			.toList();

		log.info("Found {} tables for datasource: {}", tableNames.size(), datasourceId);
		return tableNames;
	}

	/**
	 * 获取数据源的数据库配置
	 * @param datasource 数据源
	 * @return 数据库配置
	 */
	@Override
	@Transactional(readOnly = true)
	public DbConfigBO getDbConfig(Datasource datasource) {
		DatasourceTypeHandler handler = datasourceTypeHandlerRegistry.getRequired(datasource.getType());
		return handler.toDbConfig(datasource);
	}

	/**
	 * 获取指定表的字段列表
	 * @param datasourceId 数据源ID
	 * @param tableName 表名
	 * @return 字段名列表
	 * @throws Exception 查询异常
	 */
	@Override
	@Transactional(readOnly = true)
	public List<String> getTableColumns(Integer datasourceId, String tableName) throws Exception {
		log.info("Getting columns for table: {} in datasource: {}", tableName, datasourceId);

		// 获取数据源信息
		Datasource datasource = this.getDatasourceById(datasourceId);
		if (datasource == null) {
			throw new RuntimeException("Datasource not found with id: " + datasourceId);
		}

		// 创建数据库配置
		DbConfigBO dbConfig = getDbConfig(datasource);

		// 创建查询参数
		DbQueryParameter queryParam = DbQueryParameter.from(dbConfig);

		// 提取schema名称
		DatasourceTypeHandler handler = datasourceTypeHandlerRegistry.getRequired(datasource.getType());
		String schemaName = handler.extractSchemaName(datasource);
		queryParam.setSchema(schemaName);
		queryParam.setTable(tableName);

		// 查询字段列表
		Accessor dbAccessor = accessorFactory.getAccessorByDbConfig(dbConfig);
		List<ColumnInfoBO> columnInfoList = dbAccessor.showColumns(dbConfig, queryParam); // 提取字段名称
		List<String> columnNames = columnInfoList.stream()
			.map(ColumnInfoBO::getName)
			.filter(name -> name != null && !name.trim().isEmpty())
			.sorted()
			.toList();

		log.info("Found {} columns for table {} in datasource: {}", columnNames.size(), tableName, datasourceId);
		return columnNames;
	}

	/**
	 * 获取数据源的逻辑外键列表
	 * @param datasourceId 数据源ID
	 * @return 逻辑外键列表
	 */
	@Override
	@Transactional(readOnly = true)
	public List<LogicalRelation> getLogicalRelations(Integer datasourceId) {
		log.info("Getting logical relations for datasource: {}", datasourceId);
		return QueryChain.of(logicalRelationMapper)
			.eq(LogicalRelation::getDatasourceId, datasourceId)
			.eq(LogicalRelation::getIsDeleted, 0)
			.orderBy(LogicalRelation::getCreatedTime, false)
			.list();
	}

	/**
	 * 添加逻辑外键
	 * @param datasourceId 数据源ID
	 * @param logicalRelation 逻辑外键
	 * @return 创建后的逻辑外键
	 */
	@Override
	public LogicalRelation addLogicalRelation(Integer datasourceId, LogicalRelation logicalRelation) {
		log.info("Adding logical relation for datasource: {}", datasourceId);

		// 设置数据源ID
		logicalRelation.setDatasourceId(datasourceId);

		// 检查是否已存在相同的外键关系
		int exists = logicalRelationMapper.checkExists(datasourceId, logicalRelation.getSourceTableName(),
				logicalRelation.getSourceColumnName(), logicalRelation.getTargetTableName(),
				logicalRelation.getTargetColumnName());

		if (exists > 0) {
			throw new RuntimeException("该逻辑外键关系已存在");
		}

		// 插入外键
		logicalRelationMapper.insert(logicalRelation);
		log.info("Logical relation added successfully with id: {}", logicalRelation.getId());

		return logicalRelation;
	}

	/**
	 * 更新逻辑外键
	 * @param datasourceId 数据源ID
	 * @param logicalRelationId 逻辑外键ID
	 * @param logicalRelation 逻辑外键
	 * @return 更新后的逻辑外键
	 */
	@Override
	public LogicalRelation updateLogicalRelation(Integer datasourceId, Integer logicalRelationId,
			LogicalRelation logicalRelation) {
		log.info("Updating logical relation: {} for datasource: {}", logicalRelationId, datasourceId);

		// 验证外键是否存在且属于该数据源
		LogicalRelation existingRelation = logicalRelationMapper.selectOneById(logicalRelationId);
		if (existingRelation == null) {
			throw new RuntimeException("逻辑外键不存在，ID: " + logicalRelationId);
		}

		if (!existingRelation.getDatasourceId().equals(datasourceId)) {
			throw new RuntimeException("逻辑外键不属于指定的数据源");
		}

		// 设置ID和数据源ID
		logicalRelation.setId(logicalRelationId);
		logicalRelation.setDatasourceId(datasourceId);

		// 更新外键
		int updated = logicalRelationMapper.updateById(logicalRelation);
		if (updated == 0) {
			throw new RuntimeException("更新逻辑外键失败");
		}

		log.info("Logical relation updated successfully: {}", logicalRelationId);

		// 返回更新后的数据
		return logicalRelationMapper.selectOneById(logicalRelationId);
	}

	/**
	 * 删除逻辑外键
	 * @param datasourceId 数据源ID
	 * @param logicalRelationId 逻辑外键ID
	 */
	@Override
	public void deleteLogicalRelation(Integer datasourceId, Integer logicalRelationId) {
		log.info("Deleting logical relation: {} for datasource: {}", logicalRelationId, datasourceId);

		// 验证外键是否属于该数据源
		LogicalRelation logicalRelation = logicalRelationMapper.selectOneById(logicalRelationId);
		if (logicalRelation == null) {
			throw new RuntimeException("逻辑外键不存在，ID: " + logicalRelationId);
		}

		if (!logicalRelation.getDatasourceId().equals(datasourceId)) {
			throw new RuntimeException("逻辑外键不属于指定的数据源");
		}

		// 删除外键（逻辑删除）
		int deleted = logicalRelationMapper.logicalDeleteById(logicalRelationId);
		if (deleted == 0) {
			throw new RuntimeException("删除逻辑外键失败");
		}

		log.info("Logical relation deleted successfully: {}", logicalRelationId);
	}

	/**
	 * 批量保存逻辑外键（替换现有所有外键）
	 * @param datasourceId 数据源ID
	 * @param logicalRelations 逻辑外键列表
	 * @return 保存后的逻辑外键列表
	 */
	@Override
	public List<LogicalRelation> saveLogicalRelations(Integer datasourceId, List<LogicalRelation> logicalRelations) {
		log.info("Saving {} logical relations for datasource: {}", logicalRelations.size(), datasourceId);

		// 获取现有的所有外键关系
		List<LogicalRelation> existingRelations = QueryChain.of(logicalRelationMapper)
			.eq(LogicalRelation::getDatasourceId, datasourceId)
			.eq(LogicalRelation::getIsDeleted, 0)
			.orderBy(LogicalRelation::getCreatedTime, false)
			.list();
		Map<Integer, LogicalRelation> existingMap = existingRelations.stream()
			.collect(Collectors.toMap(LogicalRelation::getId, relation -> relation));

		// 收集传入列表中已存在的ID
		Set<Integer> incomingIds = logicalRelations.stream()
			.map(LogicalRelation::getId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		// 删除那些不在传入列表中的外键
		int deletedCount = 0;
		for (LogicalRelation existing : existingRelations) {
			if (!incomingIds.contains(existing.getId())) {
				logicalRelationMapper.logicalDeleteById(existing.getId());
				deletedCount++;
				log.info("Deleted logical relation: {} -> {}", existing.getSourceTableName(),
						existing.getTargetTableName());
			}
		}
		log.info("Deleted {} logical relations for datasource: {}", deletedCount, datasourceId);

		// 去重检查
		List<LogicalRelation> uniqueRelations = new ArrayList<>();
		Set<String> seen = new HashSet<>();

		for (LogicalRelation logicalRelation : logicalRelations) {
			String key = logicalRelation.getSourceTableName() + "|" + logicalRelation.getSourceColumnName() + "|"
					+ logicalRelation.getTargetTableName() + "|" + logicalRelation.getTargetColumnName();

			if (!seen.contains(key)) {
				seen.add(key);
				uniqueRelations.add(logicalRelation);
			}
			else {
				log.warn("跳过重复的逻辑外键: {} -> {}", logicalRelation.getSourceTableName(),
						logicalRelation.getTargetTableName());
			}
		}

		int duplicateCount = logicalRelations.size() - uniqueRelations.size();
		if (duplicateCount > 0) {
			log.warn("检测到并去重了 {} 条重复的逻辑外键", duplicateCount);
		}

		// 插入或更新去重后的外键列表
		int insertedCount = 0;
		int updatedCount = 0;
		for (LogicalRelation logicalRelation : uniqueRelations) {
			logicalRelation.setDatasourceId(datasourceId);

			if (logicalRelation.getId() != null && existingMap.containsKey(logicalRelation.getId())) {
				// 更新现有记录
				logicalRelationMapper.updateById(logicalRelation);
				updatedCount++;
				log.debug("Updated logical relation: {} -> {}", logicalRelation.getSourceTableName(),
						logicalRelation.getTargetTableName());
			}
			else {
				// 插入新记录
				logicalRelation.setId(null);
				logicalRelationMapper.insert(logicalRelation);
				insertedCount++;
				log.debug("Inserted logical relation: {} -> {}", logicalRelation.getSourceTableName(),
						logicalRelation.getTargetTableName());
			}
		}

		log.info("Saved logical relations for datasource {}: {} inserted, {} updated, {} deleted", datasourceId,
				insertedCount, updatedCount, deletedCount);

		return QueryChain.of(logicalRelationMapper)
			.eq(LogicalRelation::getDatasourceId, datasourceId)
			.eq(LogicalRelation::getIsDeleted, 0)
			.orderBy(LogicalRelation::getCreatedTime, false)
			.list();
	}

}
