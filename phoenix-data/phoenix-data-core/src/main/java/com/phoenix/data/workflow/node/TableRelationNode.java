package com.phoenix.data.workflow.node;

import com.phoenix.data.bo.DbConfigBO;
import com.phoenix.data.dto.schema.SchemaDTO;
import com.phoenix.data.dto.schema.TableDTO;
import com.phoenix.data.entity.AgentDatasource;
import com.phoenix.data.entity.LogicalRelation;
import com.phoenix.data.entity.SemanticModel;
import com.phoenix.data.enums.TextType;
import com.phoenix.data.service.datasource.AgentDatasourceService;
import com.phoenix.data.service.datasource.DatasourceService;
import com.phoenix.data.service.nl2sql.Nl2SqlService;
import com.phoenix.data.service.schema.SchemaService;
import com.phoenix.data.service.semantic.SemanticModelService;
import com.phoenix.data.util.ChatResponseUtil;
import com.phoenix.data.utils.DatabaseUtil;
import com.phoenix.data.utils.FluxUtil;
import com.phoenix.data.util.StateUtil;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.phoenix.data.constant.Constant.*;
import static com.phoenix.data.prompt.PromptHelper.buildSemanticModelPrompt;

/**
 * Table relationship inference node that automatically completes complex structures like
 * JOINs and foreign keys.
 *
 * <p>
 * This node is responsible for: - Inferring relationships between tables and fields -
 * Building initial schema from documents - Processing schema selection based on input and
 * evidence - Handling schema advice for missing information
 *
 * @author zhangshenghang
 */
@Slf4j
@Component
@AllArgsConstructor
public class TableRelationNode extends AabstractNodeAction{

	private final SchemaService schemaService;

	private final Nl2SqlService nl2SqlService;

	private final SemanticModelService semanticModelService;

	private final DatabaseUtil databaseUtil;

	private final DatasourceService datasourceService;

	private final AgentDatasourceService agentDatasourceService;

	@Override
	public String getChName() {
		return "表关系推理";
	}

	/**
	 * 执行表关系推断：构建初始 Schema，处理 Schema 选择，获取语义模型。
	 *
	 * @param state 全局状态
	 * @return 包含 Schema 输出和语义模型的结果映射
	 */
	@Override
	public Map<String, Object> apply(OverAllState state) throws Exception {

		// Get necessary input parameters
		String canonicalQuery = StateUtil.getCanonicalQuery(state);

		String evidence = StateUtil.getStringValue(state, EVIDENCE);
		List<Document> tableDocuments = StateUtil.getDocumentList(state, TABLE_DOCUMENTS_FOR_SCHEMA_OUTPUT);
		List<Document> columnDocuments = StateUtil.getDocumentList(state, COLUMN_DOCUMENTS__FOR_SCHEMA_OUTPUT);
		String agentIdStr = StateUtil.getStringValue(state, AGENT_ID);

		// Execute business logic first - get final result immediately
		DbConfigBO agentDbConfig = databaseUtil.getAgentDbConfig(Long.valueOf(agentIdStr));

		List<String> logicalForeignKeys = getLogicalForeignKeys(Long.valueOf(agentIdStr), tableDocuments);
		log.info("Found {} logical foreign keys for agent: {}", logicalForeignKeys.size(), agentIdStr);

		SchemaDTO initialSchema = buildInitialSchema(agentIdStr, columnDocuments, tableDocuments, agentDbConfig,
				logicalForeignKeys);

		Map<String, Object> resultMap = new HashMap<>();
		// 将 DB_DIALECT_TYPE 添加到 resultMap，确保它在 generator 完成时被写入 state
		resultMap.put(DB_DIALECT_TYPE, agentDbConfig.getDialectType());
		resultMap.put(TABLE_RELATION_RETRY_COUNT, 0);
		resultMap.put(TABLE_RELATION_EXCEPTION_OUTPUT, "");

		Flux<ChatResponse> schemaFlux = processSchemaSelection(initialSchema, canonicalQuery, evidence, state,
				agentDbConfig, result -> {
					log.info("[{}] Schema processing result: {}", this.getClass().getSimpleName(), result);
					resultMap.put(TABLE_RELATION_OUTPUT, result);

					// 从最终的SchemaDTO中获取表名列表
					List<String> tableNames = result.getTable().stream().map(TableDTO::getName).toList();

					// 根据agentId和表名列表获取语义模型
					List<SemanticModel> semanticModels = semanticModelService
						.getByAgentIdAndTableNames(Long.valueOf(agentIdStr), tableNames);

					// 构建语义模型提示并存储到resultMap中
					String semanticModelPrompt = buildSemanticModelPrompt(semanticModels);
					resultMap.put(GENEGRATED_SEMANTIC_MODEL_PROMPT, semanticModelPrompt);
				});

		// Create display stream for user experience only
		Flux<ChatResponse> preFlux = Flux.create(emitter -> {
			emitter.next(ChatResponseUtil.createResponse("开始构建初始Schema..."));
			emitter.next(ChatResponseUtil.createResponse("初始Schema构建完成."));
			emitter.complete();
		});
		Flux<ChatResponse> displayFlux = preFlux.concatWith(schemaFlux).concatWith(Flux.create(emitter -> {
			emitter.next(ChatResponseUtil.createResponse("开始处理Schema选择..."));
			emitter.next(ChatResponseUtil.createResponse("Schema选择处理完成."));
			emitter.complete();
		}));

		// Use utility class to create generator, directly return business logic computed
		// result
		Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGeneratorWithMessages(this.getChName(), this.getClass(),
				state, v -> resultMap, displayFlux);

		// Return generator and essential state values that need to be available
		// immediately
		// DB_DIALECT_TYPE must be returned directly so it's available in state for
		// subsequent nodes
		return Map.of(TABLE_RELATION_OUTPUT, generator, DB_DIALECT_TYPE, agentDbConfig.getDialectType(),
				TABLE_RELATION_RETRY_COUNT, 0, TABLE_RELATION_EXCEPTION_OUTPUT, "");
	}

	/**
	 * 从列文档和表文档构建初始 Schema，合并逻辑外键信息。
	 *
	 * @param agentId 智能体 ID
	 * @param columnDocuments 列文档列表
	 * @param tableDocuments 表文档列表
	 * @param agentDbConfig 数据库配置
	 * @param logicalForeignKeys 逻辑外键列表
	 * @return 构建的 Schema DTO
	 */
	private SchemaDTO buildInitialSchema(String agentId, List<Document> columnDocuments, List<Document> tableDocuments,
			DbConfigBO agentDbConfig, List<String> logicalForeignKeys) {
		SchemaDTO schemaDTO = new SchemaDTO();

		schemaService.extractDatabaseName(schemaDTO, agentDbConfig);
		schemaService.buildSchemaFromDocuments(agentId, columnDocuments, tableDocuments, schemaDTO);

		// 将逻辑外键信息合并到 schemaDTO 的 foreignKeys 字段
		if (logicalForeignKeys != null && !logicalForeignKeys.isEmpty()) {
			List<String> existingForeignKeys = schemaDTO.getForeignKeys();
			if (existingForeignKeys == null || existingForeignKeys.isEmpty()) {
				// 如果没有现有外键，直接设置
				schemaDTO.setForeignKeys(logicalForeignKeys);
			}
			else {
				// 合并现有外键和逻辑外键
				List<String> allForeignKeys = new ArrayList<>(existingForeignKeys);
				allForeignKeys.addAll(logicalForeignKeys);
				schemaDTO.setForeignKeys(allForeignKeys);
			}
			log.info("Merged {} logical foreign keys into schema for agent: {}", logicalForeignKeys.size(), agentId);
		}

		return schemaDTO;
	}

	/**
	 * 处理 Schema 选择：根据输入、证据和可选的 Schema 建议进行精细选择。
	 *
	 * @param schemaDTO 初始 Schema DTO
	 * @param input 用户输入
	 * @param evidence 证据信息
	 * @param state 全局状态
	 * @param agentDbConfig 数据库配置
	 * @param dtoConsumer Schema DTO 消费者回调
	 * @return LLM 响应流
	 */
	private Flux<ChatResponse> processSchemaSelection(SchemaDTO schemaDTO, String input, String evidence,
			OverAllState state, DbConfigBO agentDbConfig, Consumer<SchemaDTO> dtoConsumer) {
		String schemaAdvice = StateUtil.getStringValue(state, SQL_GENERATE_SCHEMA_MISSING_ADVICE, null);

		Flux<ChatResponse> schemaFlux;
		if (schemaAdvice != null) {
			log.info("[{}] Processing with schema supplement advice: {}", this.getClass().getSimpleName(),
					schemaAdvice);
			schemaFlux = nl2SqlService.fineSelect(schemaDTO, input, evidence, schemaAdvice, agentDbConfig, dtoConsumer);
		}
		else {
			log.info("[{}] Executing regular schema selection", this.getClass().getSimpleName());
			schemaFlux = nl2SqlService.fineSelect(schemaDTO, input, evidence, null, agentDbConfig, dtoConsumer);
		}
		return Flux
			.just(ChatResponseUtil.createResponse("正在选择合适的数据表...\n"),
					ChatResponseUtil.createPureResponse(TextType.JSON.getStartSign()))
			.concatWith(schemaFlux)
			.concatWith(Flux.just(ChatResponseUtil.createPureResponse(TextType.JSON.getEndSign()),
					ChatResponseUtil.createResponse("\n\n选择数据表完成。")));
	}

	/**
	 * 获取逻辑外键信息，过滤只保留与当前召回表相关的外键。
	 *
	 * @param agentId 智能体 ID
	 * @param tableDocuments 召回的表文档列表
	 * @return 格式化后的外键列表
	 */
	private List<String> getLogicalForeignKeys(Long agentId, List<Document> tableDocuments) {
		try {
			// 获取当前 agent 激活的数据源
			AgentDatasource agentDatasource = agentDatasourceService.getCurrentAgentDatasource(agentId);
			if (agentDatasource == null || agentDatasource.getDatasourceId() == null) {
				log.warn("No active datasource found for agent: {}", agentId);
				return Collections.emptyList();
			}

			Integer datasourceId = agentDatasource.getDatasourceId();

			// 从 tableDocuments 提取表名列表
			Set<String> recalledTableNames = tableDocuments.stream()
				.map(doc -> (String) doc.getMetadata().get("name"))
				.filter(name -> name != null && !name.isEmpty())
				.collect(Collectors.toSet());

			log.info("Recalled table names for agent {}: {}", agentId, recalledTableNames);

			// 查询该数据源的所有逻辑外键
			List<LogicalRelation> allLogicalRelations = datasourceService.getLogicalRelations(datasourceId);
			log.info("Found {} logical relations in datasource: {}", allLogicalRelations.size(), datasourceId);

			// 过滤只保留与召回表相关的外键（源表或目标表在召回列表中）
			List<String> formattedForeignKeys = allLogicalRelations.stream()
				.filter(lr -> recalledTableNames.contains(lr.getSourceTableName())
						|| recalledTableNames.contains(lr.getTargetTableName()))
				.map(lr -> String.format("%s.%s=%s.%s", lr.getSourceTableName(), lr.getSourceColumnName(),
						lr.getTargetTableName(), lr.getTargetColumnName()))
				.distinct()
				.collect(Collectors.toList());

			log.info("Filtered {} relevant logical relations for recalled tables", formattedForeignKeys.size());
			return formattedForeignKeys;
		}
		catch (Exception e) {
			log.error("Error fetching logical foreign keys for agent: {}", agentId, e);
			return Collections.emptyList();
		}
	}

}
