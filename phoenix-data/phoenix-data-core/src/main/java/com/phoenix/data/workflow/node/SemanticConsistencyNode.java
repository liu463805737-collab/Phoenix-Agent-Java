package com.phoenix.data.workflow.node;

import com.phoenix.data.dto.datasource.SqlRetryDto;
import com.phoenix.data.dto.prompt.SemanticConsistencyDTO;
import com.phoenix.data.dto.schema.SchemaDTO;
import com.phoenix.data.service.nl2sql.Nl2SqlService;
import com.phoenix.data.utils.FluxUtil;
import com.phoenix.data.util.StateUtil;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

import static com.phoenix.data.constant.Constant.*;
import static com.phoenix.data.prompt.PromptHelper.buildMixMacSqlDbPrompt;
import static com.phoenix.data.util.PlanProcessUtil.getCurrentExecutionStepInstruction;

/**
 * Semantic consistency validation node that checks SQL query semantic consistency.
 *
 * This node is responsible for: - Validating SQL query semantic consistency against
 * schema and evidence - Providing validation results for query refinement - Handling
 * validation failures with recommendations - Managing step progression in execution plan
 *
 * @author zhangshenghang
 */
@Slf4j
@Component
@AllArgsConstructor
public class SemanticConsistencyNode extends AabstractNodeAction{

	private final Nl2SqlService nl2SqlService;

	@Override
	public String getChName() {
		return "验证SQL";
	}

	/**
	 * 执行语义一致性校验：验证 SQL 查询与 Schema、证据的语义一致性。
	 *
	 * @param state 全局状态
	 * @return 包含校验结果的结果映射
	 */
	@Override
	public Map<String, Object> apply(OverAllState state) throws Exception {

		// Get necessary input parameters
		String evidence = StateUtil.getStringValue(state, EVIDENCE);
		SchemaDTO schemaDTO = StateUtil.getObjectValue(state, TABLE_RELATION_OUTPUT, SchemaDTO.class);
		String dialect = StateUtil.getStringValue(state, DB_DIALECT_TYPE);
		// Get current execution step and SQL query
		String sql = StateUtil.getStringValue(state, SQL_GENERATE_OUTPUT);
		String userQuery = StateUtil.getCanonicalQuery(state);

		SemanticConsistencyDTO semanticConsistencyDTO = SemanticConsistencyDTO.builder()
			.dialect(dialect)
			.sql(sql)
			.executionDescription(getCurrentExecutionStepInstruction(state))
			.schemaInfo(buildMixMacSqlDbPrompt(schemaDTO, true))
			.userQuery(userQuery)
			.evidence(evidence)
			.build();
		log.info("Starting semantic consistency validation - SQL: {}", sql);
		Flux<ChatResponse> validationResultFlux = nl2SqlService.performSemanticConsistency(semanticConsistencyDTO);

		Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGeneratorWithMessages(this.getChName(), this.getClass(),
				state, "开始语义一致性校验", "语义一致性校验完成", validationResult -> {
					boolean isPassed = !validationResult.startsWith("不通过");
					Map<String, Object> result = buildValidationResult(isPassed, validationResult);
					log.info("[{}] Semantic consistency validation result: {}, passed: {}",
							this.getClass().getSimpleName(), validationResult, isPassed);
					return result;
				}, validationResultFlux);

		return Map.of(SEMANTIC_CONSISTENCY_NODE_OUTPUT, generator);
	}

	/**
	 * 构建校验结果映射，通过则标记为 true，否则附带重试原因。
	 *
	 * @param passed 是否通过校验
	 * @param validationResult 校验结果描述
	 * @return 校验结果映射
	 */
	private Map<String, Object> buildValidationResult(boolean passed, String validationResult) {
		if (passed) {
			return Map.of(SEMANTIC_CONSISTENCY_NODE_OUTPUT, true);
		}
		else {
			return Map.of(SEMANTIC_CONSISTENCY_NODE_OUTPUT, false, SQL_REGENERATE_REASON,
					SqlRetryDto.semantic(validationResult));
		}
	}

}
