package com.phoenix.data.service.nl2sql;

import com.phoenix.data.bo.DbConfigBO;
import com.phoenix.data.dto.prompt.SemanticConsistencyDTO;
import com.phoenix.data.dto.prompt.SqlGenerationDTO;
import com.phoenix.data.dto.schema.SchemaDTO;
import com.phoenix.data.prompt.PromptHelper;
import com.phoenix.data.service.llm.LlmService;
import com.phoenix.data.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.phoenix.data.utils.FluxUtil;
import com.phoenix.data.utils.JsonParseUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.phoenix.data.prompt.PromptHelper.buildMixMacSqlDbPrompt;
import static com.phoenix.data.prompt.PromptHelper.buildMixSelectorPrompt;

/**
 * NL2SQL 服务实现类，提供语义一致性校验、SQL 生成和精细化表选择等能力。
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@AllArgsConstructor
public class Nl2SqlServiceImpl implements Nl2SqlService {

	public final LlmService llmService;

	private final JsonParseUtil jsonParseUtil;

	/**
	 * 执行语义一致性校验，构建提示词并调用 LLM
	 * @param semanticConsistencyDTO 语义一致性校验参数
	 * @return ChatResponse 响应流
	 */
	@Override
	public Flux<ChatResponse> performSemanticConsistency(SemanticConsistencyDTO semanticConsistencyDTO) {
		String semanticConsistencyPrompt = PromptHelper.buildSemanticConsistenPrompt(semanticConsistencyDTO);
		log.debug("semanticConsistencyPrompt as follows \n {} \n", semanticConsistencyPrompt);
		return llmService.callUser(semanticConsistencyPrompt);
	}

	/**
	 * 生成或修复 SQL 语句，根据是否已有 SQL 决定走修复或新建流程
	 * @param sqlGenerationDTO SQL 生成参数
	 * @return SQL 文本流
	 */
	@Override
	public Flux<String> generateSql(SqlGenerationDTO sqlGenerationDTO) {
		String sql = sqlGenerationDTO.getSql();
		log.info("Generating SQL for query: {}, hasExistingSql: {}, dialect: {}",
				sqlGenerationDTO.getExecutionDescription(), StringUtils.hasText(sql), sqlGenerationDTO.getDialect());

		Flux<String> newSqlFlux;
		if (sql != null && !sql.isEmpty()) {
			// Use professional SQL error repair prompt
			log.debug("Using SQL error fixer for existing SQL: {}", sql);
			String errorFixerPrompt = PromptHelper.buildSqlErrorFixerPrompt(sqlGenerationDTO);
			log.debug("SQL error fixer prompt as follows \n {} \n", errorFixerPrompt);
			newSqlFlux = llmService.toStringFlux(llmService.callUser(errorFixerPrompt));
			log.info("SQL error fixing completed");
		}
		else {
			// Normal SQL generation process
			log.debug("Generating new SQL from scratch");
			String prompt = PromptHelper.buildNewSqlGeneratorPrompt(sqlGenerationDTO);
			log.debug("New SQL generator prompt as follows \n {} \n", prompt);
			newSqlFlux = llmService.toStringFlux(llmService.callSystem(prompt));
			log.info("New SQL generation completed");
		}

		return newSqlFlux;
	}

	/**
	 * 根据缺失表建议进行精细化表选择
	 * @param schemaDTO 数据库模式信息
	 * @param sqlGenerateSchemaMissingAdvice 缺失表建议
	 * @param resultConsumer 表名结果回调
	 * @return ChatResponse 响应流
	 */
	private Flux<ChatResponse> fineSelect(SchemaDTO schemaDTO, String sqlGenerateSchemaMissingAdvice,
			Consumer<Set<String>> resultConsumer) {
		log.debug("Fine selecting tables based on advice: {}", sqlGenerateSchemaMissingAdvice);
		String schemaInfo = buildMixMacSqlDbPrompt(schemaDTO, true);
		String prompt = " 建议：" + sqlGenerateSchemaMissingAdvice
				+ " \n 请按照建议进行返回相关表的名称，只返回建议中提到的表名，返回格式为：[\"a\",\"b\",\"c\"] \n " + schemaInfo;
		log.debug("Built table selection with advice prompt as follows \n {} \n", prompt);
		StringBuilder sb = new StringBuilder();
		return llmService.callUser(prompt).doOnNext(r -> {
			String text = r.getResult().getOutput().getText();
			sb.append(text);
		}).doOnComplete(() -> {
			String content = sb.toString();
			if (!content.trim().isEmpty()) {
				String jsonContent = MarkdownParserUtil.extractText(content);
				List<String> tableList;
				try {
					tableList = JsonUtil.getObjectMapper().readValue(jsonContent, new TypeReference<List<String>>() {
					});
				}
				catch (Exception e) {
					log.error("Failed to parse table selection response: {}", jsonContent, e);
					throw new IllegalStateException(jsonContent);
				}
				if (tableList != null && !tableList.isEmpty()) {
					Set<String> selectedTables = tableList.stream()
						.map(String::toLowerCase)
						.collect(Collectors.toSet());
					log.debug("Selected {} tables based on advice: {}", selectedTables.size(), selectedTables);
					resultConsumer.accept(selectedTables);
				}
			}
			log.debug("No tables selected based on advice");
			resultConsumer.accept(new HashSet<>());
		});
	}

	/**
	 * 精细化选择数据库表结构，结合证据和缺失表建议进行表过滤
	 * @param schemaDTO 数据库模式信息
	 * @param query 用户查询
	 * @param evidence 证据信息
	 * @param sqlGenerateSchemaMissingAdvice 缺失表建议
	 * @param specificDbConfig 指定数据库配置
	 * @param dtoConsumer 结果回调
	 * @return ChatResponse 响应流
	 */
	@Override
	public Flux<ChatResponse> fineSelect(SchemaDTO schemaDTO, String query, String evidence,
			String sqlGenerateSchemaMissingAdvice, DbConfigBO specificDbConfig, Consumer<SchemaDTO> dtoConsumer) {
		log.debug("Fine selecting schema for query: {} with evidences and specificDbConfig: {}", query,
				specificDbConfig != null ? specificDbConfig.getUrl() : "default");

		String prompt = buildMixSelectorPrompt(evidence, query, schemaDTO);
		log.debug("Built schema fine selection prompt as follows \n {} \n", prompt);

		Set<String> selectedTables = new HashSet<>();

		return FluxUtil.<ChatResponse, String>cascadeFlux(llmService.callUser(prompt), content -> {
			Flux<ChatResponse> nextFlux;
			if (sqlGenerateSchemaMissingAdvice != null) {
				log.debug("Adding tables from schema missing advice");
				nextFlux = this.fineSelect(schemaDTO, sqlGenerateSchemaMissingAdvice, selectedTables::addAll);
			}
			else {
				nextFlux = Flux.empty();
			}
			return nextFlux.doOnComplete(() -> {
				if (!content.trim().isEmpty()) {
					String jsonContent = MarkdownParserUtil.extractText(content);
					List<String> tableList;
					try {
						tableList = jsonParseUtil.tryConvertToObject(jsonContent, new TypeReference<List<String>>() {
						});
					}
					catch (Exception e) {
						// Some scenarios may prompt exceptions, such as:
						// java.lang.IllegalStateException:
						// Please provide database schema information so I can filter
						// relevant
						// tables based on your question.
						// TODO 目前异常接口直接返回500，未返回异常信息，后续优化将异常返回给用户
						log.error("Failed to parse fine selection response: {}", jsonContent, e);
						throw new IllegalStateException(jsonContent);
					}
					if (tableList != null && !tableList.isEmpty()) {
						selectedTables.addAll(tableList.stream().map(String::toLowerCase).collect(Collectors.toSet()));
						if (schemaDTO.getTable() != null) {
							int originalTableCount = schemaDTO.getTable().size();
							schemaDTO.getTable()
								.removeIf(table -> !selectedTables.contains(table.getName().toLowerCase()));
							int finalTableCount = schemaDTO.getTable().size();
							log.debug("Fine selection completed: {} -> {} tables, selected tables: {}",
									originalTableCount, finalTableCount, selectedTables);
						}
					}
				}
				dtoConsumer.accept(schemaDTO);
			});
		}, flux -> flux.map(ChatResponseUtil::getText)
			.collect(StringBuilder::new, StringBuilder::append)
			.map(StringBuilder::toString));
	}

}
