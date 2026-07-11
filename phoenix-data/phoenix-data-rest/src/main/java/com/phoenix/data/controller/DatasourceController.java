package com.phoenix.data.controller;

import com.phoenix.data.dto.datasource.DatasourceTypeDTO;
import com.phoenix.data.dto.schema.CreateLogicalRelationDTO;
import com.phoenix.data.dto.schema.UpdateLogicalRelationDTO;
import com.phoenix.data.entity.Datasource;
import com.phoenix.data.entity.LogicalRelation;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import com.phoenix.data.exception.InternalServerException;
import com.phoenix.data.exception.InvalidInputException;
import com.phoenix.data.service.datasource.DatasourceService;
import com.phoenix.data.vo.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据源控制器
 */
// todo: 不要吞掉所有异常，可以直接抛出，写一个Advice拦截异常并做日志
@Slf4j
@RestController
@RequestMapping("/api/datasource")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class DatasourceController {

	private final DatasourceService datasourceService;

	/**
	 * Get all data source list
	 */
	@GetMapping("/types")
	public ApiResponse<List<DatasourceTypeDTO>> getDatasourceTypes() {
		// 定义标准的 JDBC 数据源类型
		List<BizDataSourceTypeEnum> standardTypes = Arrays.asList(BizDataSourceTypeEnum.MYSQL,
				BizDataSourceTypeEnum.POSTGRESQL, BizDataSourceTypeEnum.DAMENG, BizDataSourceTypeEnum.SQL_SERVER,
				BizDataSourceTypeEnum.ORACLE, BizDataSourceTypeEnum.HIVE);

		List<DatasourceTypeDTO> types = standardTypes.stream()
			.map(type -> DatasourceTypeDTO.builder()
				.code(type.getCode())
				.typeName(type.getTypeName())
				.dialect(type.getDialect())
				.protocol(type.getProtocol())
				.displayName(type.getDialect()) // 使用 dialect 作为显示名称
				.build())
			.collect(Collectors.toList());

		return ApiResponse.success("获取数据源类型成功", types);
	}

	/**
	 * Get all data source list
	 */
	@GetMapping
	public List<Datasource> getAllDatasource(@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "type", required = false) String type) {

		List<Datasource> result;

		if (StringUtils.isNotBlank(status)) {
			result = datasourceService.getDatasourceByStatus(status);
		}
		else if (StringUtils.isNotBlank(type)) {
			result = datasourceService.getDatasourceByType(type);
		}
		else {
			result = datasourceService.getAllDatasource();
		}

		return result;
	}

	/**
	 * Get data source details by ID
	 */
	@GetMapping("/{id}")
	public Datasource getDatasourceById(@PathVariable Integer id) {
		return checkDatasourceExists(id);
	}

	/**
	 * 获取数据源的表列表
	 *
	 * @param id 数据源ID
	 * @return 表名列表
	 */
	@GetMapping("/{id}/tables")
	public List<String> getDatasourceTables(@PathVariable Integer id) {
		checkDatasourceExists(id);
		try {
			return datasourceService.getDatasourceTables(id);
		}
		catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * Create data source
	 */
	@PostMapping
	public Datasource createDatasource(@RequestBody Datasource datasource) {
		try {
			return datasourceService.createDatasource(datasource);
		}
		catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * Update data source
	 */
	@PutMapping("/{id}")
	public Datasource updateDatasource(@PathVariable Integer id, @RequestBody Datasource datasource) {
		checkDatasourceExists(id);
		try {
			return datasourceService.updateDatasource(id, datasource);
		}
		catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * Delete data source
	 */
	@DeleteMapping("/{id}")
	public ApiResponse deleteDatasource(@PathVariable Integer id) {
		try {
			checkDatasourceExists(id);
			datasourceService.deleteDatasource(id);
			return ApiResponse.success("数据源删除成功");
		}
		catch (ResponseStatusException e) {
			throw new InvalidInputException(e.getMessage());
		}
		catch (Exception e) {
			throw new InternalServerException("删除失败：" + e.getMessage());
		}
	}

	/**
	 * Test data source connection
	 */
	@PostMapping("/{id}/test")
	public ApiResponse testConnection(@PathVariable Integer id) {
		try {
			boolean success = datasourceService.testConnection(id);
			return success ? ApiResponse.success("连接测试成功") : ApiResponse.error("连接测试失败");
		}
		catch (Exception e) {
			throw new InternalServerException("测试失败：" + e.getMessage());
		}
	}

	/**
	 * 获取数据源表的字段列表
	 */
	@GetMapping("/{id}/tables/{tableName}/columns")
	public ApiResponse<List<String>> getTableColumns(@PathVariable Integer id, @PathVariable String tableName) {
		try {
			List<String> columns = datasourceService.getTableColumns(id, tableName);
			return ApiResponse.success("获取字段列表成功", columns);
		}
		catch (Exception e) {
			throw new InternalServerException("获取字段列表失败：" + e.getMessage());
		}
	}

	/**
	 * 获取数据源的逻辑外键列表
	 */
	@GetMapping("/{id}/logical-relations")
	public ApiResponse<List<LogicalRelation>> getLogicalRelations(@PathVariable(value = "id") Integer datasourceId) {
		try {
			List<LogicalRelation> logicalRelations = datasourceService.getLogicalRelations(datasourceId);
			return ApiResponse.success("success get logical relations", logicalRelations);
		}
		catch (Exception e) {
			log.error("Failed to get logical relations for datasource: {}", datasourceId, e);
			throw new InternalServerException("获取逻辑外键失败：" + e.getMessage());
		}
	}

	/**
	 * 添加逻辑外键
	 */
	@PostMapping("/{id}/logical-relations")
	public ApiResponse<LogicalRelation> addLogicalRelation(@PathVariable(value = "id") Integer datasourceId,
			@Valid @RequestBody CreateLogicalRelationDTO dto) {
		try {
			LogicalRelation logicalRelation = LogicalRelation.builder()
				.sourceTableName(dto.getSourceTableName())
				.sourceColumnName(dto.getSourceColumnName())
				.targetTableName(dto.getTargetTableName())
				.targetColumnName(dto.getTargetColumnName())
				.relationType(dto.getRelationType())
				.description(dto.getDescription())
				.build();

			LogicalRelation created = datasourceService.addLogicalRelation(datasourceId, logicalRelation);
			return ApiResponse.success("success create logical relation", created);
		}
		catch (Exception e) {
			log.error("Failed to add logical relation for datasource: {}", datasourceId, e);
			throw new InternalServerException("添加逻辑外键失败：" + e.getMessage());
		}
	}

	/**
	 * 更新逻辑外键
	 */
	@PutMapping("/{id}/logical-relations/{relationId}")
	public ApiResponse<LogicalRelation> updateLogicalRelation(@PathVariable(value = "id") Integer datasourceId,
			@PathVariable Integer relationId, @RequestBody UpdateLogicalRelationDTO dto) {
		try {
			LogicalRelation logicalRelation = LogicalRelation.builder()
				.sourceTableName(dto.getSourceTableName())
				.sourceColumnName(dto.getSourceColumnName())
				.targetTableName(dto.getTargetTableName())
				.targetColumnName(dto.getTargetColumnName())
				.relationType(dto.getRelationType())
				.description(dto.getDescription())
				.build();

			LogicalRelation updated = datasourceService.updateLogicalRelation(datasourceId, relationId,
					logicalRelation);
			return ApiResponse.success("success update logical relation", updated);
		}
		catch (Exception e) {
			log.error("Failed to update logical relation {} for datasource: {}", relationId, datasourceId, e);
			throw new InternalServerException("更新逻辑外键失败：" + e.getMessage());
		}
	}

	/**
	 * 删除逻辑外键
	 */
	@DeleteMapping("/{id}/logical-relations/{relationId}")
	public ApiResponse<Void> deleteLogicalRelation(@PathVariable(value = "id") Integer datasourceId,
			@PathVariable Integer relationId) {
		try {
			datasourceService.deleteLogicalRelation(datasourceId, relationId);
			return ApiResponse.success("success delete logical relation");
		}
		catch (Exception e) {
			log.error("Failed to delete logical relation {} for datasource: {}", relationId, datasourceId, e);
			throw new InternalServerException("删除逻辑外键失败：" + e.getMessage());
		}
	}

	/**
	 * 批量保存逻辑外键（替换现有的所有外键）
	 */
	@PutMapping("/{id}/logical-relations")
	public ApiResponse<List<LogicalRelation>> saveLogicalRelations(@PathVariable(value = "id") Integer datasourceId,
			@RequestBody List<LogicalRelation> logicalRelations) {
		try {
			List<LogicalRelation> saved = datasourceService.saveLogicalRelations(datasourceId, logicalRelations);
			return ApiResponse.success("success save logical relations", saved);
		}
		catch (Exception e) {
			log.error("Failed to save logical relations for datasource: {}", datasourceId, e);
			throw new InternalServerException("批量保存逻辑外键失败：" + e.getMessage());
		}
	}

	/**
	 * 检查数据源是否存在，不存在则抛出异常
	 *
	 * @param id 数据源ID
	 * @return 数据源实体
	 */
	private Datasource checkDatasourceExists(Integer id) {
		Datasource datasource = datasourceService.getDatasourceById(id);
		if (datasource == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Datasource: [%s] not found".formatted(id));
		}
		return datasource;
	}

}
