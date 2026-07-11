package com.phoenix.data.service.semantic;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.data.dto.schema.SemanticModelAddDTO;
import com.phoenix.data.dto.schema.SemanticModelBatchImportDTO;
import com.phoenix.data.dto.schema.SemanticModelImportItem;
import com.phoenix.data.entity.AgentDatasource;
import com.phoenix.data.entity.SemanticModel;
import com.phoenix.data.mapper.AgentDatasourceMapper;
import com.phoenix.data.mapper.SemanticModelMapper;
import com.phoenix.data.vo.BatchImportResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class SemanticModelServiceImpl extends ServiceImpl<SemanticModelMapper, SemanticModel> implements SemanticModelService {

	private final AgentDatasourceMapper agentDatasourceMapper;

	private final SemanticModelExcelService excelService;

	public SemanticModelServiceImpl(AgentDatasourceMapper agentDatasourceMapper, SemanticModelExcelService excelService) {
		this.agentDatasourceMapper = agentDatasourceMapper;
		this.excelService = excelService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<SemanticModel> getAll() {
		return list();
	}

	@Override
	@Transactional(readOnly = true)
	public List<SemanticModel> getEnabledByAgentId(Long agentId) {
		return QueryChain.of(getMapper())
			.eq(SemanticModel::getAgentId, agentId)
			.ne(SemanticModel::getStatus, 0)
			.orderBy(SemanticModel::getCreatedTime, false)
			.list();
	}

	@Override
	@Transactional(readOnly = true)
	public List<SemanticModel> getByAgentIdAndTableNames(Long agentId, List<String> tableNames) {
		Integer datasourceId = findDatasourceIdByAgentId(agentId);

		if (datasourceId == null || tableNames == null || tableNames.isEmpty()) {
			return List.of();
		}

		return getMapper().selectByDatasourceIdAndTableNames(datasourceId, tableNames);
	}

	@Override
	@Transactional(readOnly = true)
	public SemanticModel getById(Long id) {
		return super.getById(id);
	}

	@Override
	public void addSemanticModel(SemanticModel semanticModel) {
		save(semanticModel);
	}

	@Override
	public boolean addSemanticModel(SemanticModelAddDTO dto) {
		Integer datasourceId = findDatasourceIdByAgentId(dto.getAgentId());

		SemanticModel semanticModel = SemanticModel.builder()
			.agentId(dto.getAgentId())
			.datasourceId(datasourceId)
			.tableName(dto.getTableName())
			.columnName(dto.getColumnName())
			.businessName(dto.getBusinessName())
			.synonyms(dto.getSynonyms())
			.businessDescription(dto.getBusinessDescription())
			.columnComment(dto.getColumnComment())
			.dataType(dto.getDataType())
			.status(1)
			.build();

		save(semanticModel);

		return true;
	}

	private Integer findDatasourceIdByAgentId(Long agentId) {
		List<AgentDatasource> agentDatasources = agentDatasourceMapper.selectListByQuery(
			new QueryWrapper().eq("agent_id", agentId).orderBy("create_time", false));

		if (agentDatasources.isEmpty()) {
			throw new RuntimeException("No datasource found for Agent ID " + agentId);
		}

		for (AgentDatasource ad : agentDatasources) {
			if (ad.getIsActive() != null && ad.getIsActive() == 1) {
				return ad.getDatasourceId();
			}
		}

		return agentDatasources.get(0).getDatasourceId();
	}

	@Override
	public void enableSemanticModel(Long id) {
		getMapper().update(SemanticModel.builder().id(id).status(1).build());
	}

	@Override
	public void disableSemanticModel(Long id) {
		getMapper().update(SemanticModel.builder().id(id).status(0).build());
	}

	@Override
	@Transactional(readOnly = true)
	public List<SemanticModel> getByAgentId(Long agentId) {
		return QueryChain.of(getMapper())
			.eq(SemanticModel::getAgentId, agentId)
			.orderBy(SemanticModel::getCreatedTime, false)
			.list();
	}

	@Override
	@Transactional(readOnly = true)
	public List<SemanticModel> search(String keyword) {
		return getMapper().searchByKeyword(keyword);
	}

	@Override
	public void deleteSemanticModel(Long id) {
		removeById(id);
	}

	@Override
	public BatchImportResult batchImport(SemanticModelBatchImportDTO dto) {
		BatchImportResult result = BatchImportResult.builder()
			.total(dto.getItems().size())
			.successCount(0)
			.failCount(0)
			.build();

		Integer datasourceId;
		try {
			datasourceId = findDatasourceIdByAgentId(dto.getAgentId());
		}
		catch (Exception e) {
			log.error("获取数据源ID失败: agentId={}", dto.getAgentId(), e);
			result.setFailCount(dto.getItems().size());
			result.addError("获取数据源ID失败: " + e.getMessage());
			return result;
		}

		for (int i = 0; i < dto.getItems().size(); i++) {
			SemanticModelImportItem item = dto.getItems().get(i);
			try {
				SemanticModel existing = getMapper().selectByAgentIdAndTableNameAndColumnName(
						dto.getAgentId().intValue(), item.getTableName(), item.getColumnName());

				if (existing != null) {
					existing.setBusinessName(item.getBusinessName());
					existing.setSynonyms(item.getSynonyms());
					existing.setBusinessDescription(item.getBusinessDescription());
					existing.setColumnComment(item.getColumnComment());
					existing.setDataType(item.getDataType());
					getMapper().update(existing);
					log.info("更新语义模型: agentId={}, tableName={}, columnName={}", dto.getAgentId(), item.getTableName(),
							item.getColumnName());
				}
				else {
					SemanticModel newModel = SemanticModel.builder()
						.agentId(dto.getAgentId())
						.datasourceId(datasourceId)
						.tableName(item.getTableName())
						.columnName(item.getColumnName())
						.businessName(item.getBusinessName())
						.synonyms(item.getSynonyms())
						.businessDescription(item.getBusinessDescription())
						.columnComment(item.getColumnComment())
						.dataType(item.getDataType())
						.status(1)
						.createdTime(
								item.getCreateTime() != null ? item.getCreateTime() : java.time.LocalDateTime.now())
						.build();
					save(newModel);
					log.info("插入语义模型: agentId={}, tableName={}, columnName={}", dto.getAgentId(), item.getTableName(),
							item.getColumnName());
				}

				result.setSuccessCount(result.getSuccessCount() + 1);
			}
			catch (Exception e) {
				log.error("导入第{}条记录失败: tableName={}, columnName={}", i + 1, item.getTableName(), item.getColumnName(),
						e);
				result.setFailCount(result.getFailCount() + 1);
				result.addError(String.format("第%d条记录失败 (%s.%s): %s", i + 1, item.getTableName(), item.getColumnName(),
						e.getMessage()));
			}
		}

		return result;
	}

	@Override
	public BatchImportResult importFromExcel(InputStream inputStream, String filename, Long agentId) {
		log.info("开始Excel导入: agentId={}, 文件名={}", agentId, filename);

		try {
			List<SemanticModelImportItem> items = excelService.parseExcel(inputStream, filename);

			SemanticModelBatchImportDTO dto = SemanticModelBatchImportDTO.builder()
				.agentId(agentId)
				.items(items)
				.build();

			BatchImportResult result = batchImport(dto);
			log.info("Excel导入完成: 总数={}, 成功={}, 失败={}", result.getTotal(), result.getSuccessCount(),
					result.getFailCount());

			return result;
		}
		catch (Exception e) {
			log.error("Excel导入失败", e);
			BatchImportResult result = BatchImportResult.builder().total(0).successCount(0).failCount(0).build();
			result.addError("Excel导入失败: " + e.getMessage());
			return result;
		}
	}

	@Override
	public void updateSemanticModel(Long id, SemanticModel semanticModel) {
		semanticModel.setId(id);
		getMapper().update(semanticModel);
	}

}
