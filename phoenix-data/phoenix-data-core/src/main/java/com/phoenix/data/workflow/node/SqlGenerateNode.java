package com.phoenix.data.workflow.node;

import com.phoenix.data.dto.datasource.SqlRetryDto;
import com.phoenix.data.dto.planner.ExecutionStep;
import com.phoenix.data.dto.prompt.SqlGenerationDTO;
import com.phoenix.data.dto.schema.SchemaDTO;
import com.phoenix.data.enums.TextType;
import com.phoenix.data.properties.DataAgentProperties;
import com.phoenix.data.service.nl2sql.Nl2SqlService;
import com.phoenix.data.util.ChatResponseUtil;
import com.phoenix.data.utils.FluxUtil;
import com.phoenix.data.util.PlanProcessUtil;
import com.phoenix.data.util.StateUtil;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

import static com.phoenix.data.constant.Constant.*;
import static com.phoenix.data.util.PlanProcessUtil.getCurrentExecutionStepInstruction;

/**
 * Enhanced SQL generation node that handles SQL query regeneration with advanced
 * optimization features. This node is responsible for: - Multi-round SQL optimization and
 * refinement - Syntax validation and security analysis - Performance optimization and
 * intelligent caching - Handling execution exceptions and semantic consistency failures -
 * Managing retry logic with schema advice - Providing streaming feedback during
 * regeneration process
 *
 * @author zhangshenghang
 */
@Slf4j
@Component
@AllArgsConstructor
public class SqlGenerateNode extends AabstractNodeAction {

	private final Nl2SqlService nl2SqlService;

	private final DataAgentProperties properties;

	@Override
	public String getChName() {
		return "生成SQL";
	}

	/**
	 * 执行 SQL 生成：根据执行步骤描述和重试原因生成或重新生成 SQL。
	 *
	 * @param state 全局状态
	 * @return 包含 SQL 生成结果的结果映射
	 */
	@Override
	public Map<String, Object> apply(OverAllState state) throws Exception {
		// 判断是否达到最大尝试次数
		int count = state.value(SQL_GENERATE_COUNT, 0);
		if (count >= properties.getMaxSqlRetryCount()) {
			ExecutionStep executionStep = PlanProcessUtil.getCurrentExecutionStep(state);
			String sqlGenerateOutput = String.format("步骤[%d]中，SQL次数生成超限，最大尝试次数：%d，已尝试次数:%d，该步骤内容: \n %s",
					executionStep.getStep(), properties.getMaxSqlRetryCount(), count,
					executionStep.getToolParameters().getInstruction());
			log.error("SQL generation failed, reason: {}", sqlGenerateOutput);
			Flux<ChatResponse> preFlux = Flux.just(ChatResponseUtil.createResponse(sqlGenerateOutput));
			Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGeneratorWithMessages(this.getChName(),
					this.getClass(), state, "正在进行重试评估...", "重试评估完成！",
					retryOutput -> Map.of(SQL_GENERATE_OUTPUT, StateGraph.END, SQL_GENERATE_COUNT, 0), preFlux);
			// reset the sql generate count
			return Map.of(SQL_GENERATE_OUTPUT, generator);
		}

		// 获取planner分配的当前执行步骤的sql任务要求，每个步骤的sql任务是不同的。
		// 不要拿 user query 这个总体的大任务。
		String promptForSql = getCurrentExecutionStepInstruction(state);

		// 准备生成SQL
		String displayMessage;
		Flux<String> sqlFlux;
		SqlRetryDto retryDto = StateUtil.getObjectValue(state, SQL_REGENERATE_REASON, SqlRetryDto.class,
				SqlRetryDto.empty());

		if (retryDto.sqlExecuteFail()) {
			displayMessage = "检测到SQL执行异常，开始重新生成SQL...";
			sqlFlux = handleRetryGenerateSql(state, StateUtil.getStringValue(state, SQL_GENERATE_OUTPUT, ""),
					retryDto.reason(), promptForSql);
		}
		else if (retryDto.semanticFail()) {
			displayMessage = "语义一致性校验未通过，开始重新生成SQL...";
			sqlFlux = handleRetryGenerateSql(state, StateUtil.getStringValue(state, SQL_GENERATE_OUTPUT, ""),
					retryDto.reason(), promptForSql);
		}
		else {
			displayMessage = "开始生成SQL...";
			sqlFlux = handleGenerateSql(state, promptForSql);
		}

		// 准备返回结果，同时需要清除一些状态数据
		Map<String, Object> result = new HashMap<>(Map.of(SQL_GENERATE_OUTPUT, StateGraph.END, SQL_GENERATE_COUNT,
				count + 1, SQL_REGENERATE_REASON, SqlRetryDto.empty()));

		// Create display flux for user experience only
		StringBuilder sqlCollector = new StringBuilder();
		Flux<ChatResponse> preFlux = Flux.just(ChatResponseUtil.createResponse(displayMessage),
				ChatResponseUtil.createPureResponse(TextType.SQL.getStartSign()));
		Flux<ChatResponse> displayFlux = preFlux
			.concatWith(sqlFlux.doOnNext(sqlCollector::append).map(ChatResponseUtil::createPureResponse))
			.concatWith(Flux.just(ChatResponseUtil.createPureResponse(TextType.SQL.getEndSign()),
					ChatResponseUtil.createResponse("SQL生成完成，准备执行")));

		Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGeneratorWithMessages(this.getChName(), this.getClass(),
				state, v -> {
					String sql = nl2SqlService.sqlTrim(sqlCollector.toString());
					result.put(SQL_GENERATE_OUTPUT, sql);
					return result;
				}, displayFlux);

		return Map.of(SQL_GENERATE_OUTPUT, generator);
	}

	/**
	 * 处理重试生成 SQL：根据错误信息重新生成。
	 *
	 * @param state 全局状态
	 * @param originalSql 原始 SQL
	 * @param errorMsg 错误信息
	 * @param executionDescription 执行描述
	 * @return SQL 字符串流
	 */
	private Flux<String> handleRetryGenerateSql(OverAllState state, String originalSql, String errorMsg,
			String executionDescription) {
		String evidence = StateUtil.getStringValue(state, EVIDENCE);
		SchemaDTO schemaDTO = StateUtil.getObjectValue(state, TABLE_RELATION_OUTPUT, SchemaDTO.class);
		String userQuery = StateUtil.getCanonicalQuery(state);
		String dialect = StateUtil.getStringValue(state, DB_DIALECT_TYPE);

		SqlGenerationDTO sqlGenerationDTO = SqlGenerationDTO.builder()
			.evidence(evidence)
			.query(userQuery)
			.schemaDTO(schemaDTO)
			.sql(originalSql)
			.exceptionMessage(errorMsg)
			.executionDescription(executionDescription)
			.dialect(dialect)
			.build();

		return nl2SqlService.generateSql(sqlGenerationDTO);
	}

	/**
	 * 处理首次 SQL 生成。
	 *
	 * @param state 全局状态
	 * @param executionDescription 执行描述
	 * @return SQL 字符串流
	 */
	private Flux<String> handleGenerateSql(OverAllState state, String executionDescription) {
		return handleRetryGenerateSql(state, null, null, executionDescription);
	}

}
