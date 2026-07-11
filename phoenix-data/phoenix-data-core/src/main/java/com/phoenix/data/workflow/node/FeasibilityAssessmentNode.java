package com.phoenix.data.workflow.node;

import com.phoenix.data.dto.schema.SchemaDTO;
import com.phoenix.data.prompt.PromptHelper;
import com.phoenix.data.service.llm.LlmService;
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

/**
 * 可行性评估节点，判断需求类型为数据分析、需要澄清或自由闲聊。
 */
@Slf4j
@Component
@AllArgsConstructor
public class FeasibilityAssessmentNode extends AabstractNodeAction{

	private final LlmService llmService;

	@Override
	public String getChName() {
		return "可行性评估";
	}

	/**
	 * 执行可行性评估：结合规范化查询、召回 Schema 和证据信息，判断需求可行性。
	 *
	 * @param state 全局状态
	 * @return 包含评估结果的结果映射
	 */
	@Override
	public Map<String, Object> apply(OverAllState state) throws Exception {
		// 获取canonical_query
		String canonicalQuery = StateUtil.getCanonicalQuery(state);

		// 获取召回的Schema
		SchemaDTO recalledSchema = StateUtil.getObjectValue(state, TABLE_RELATION_OUTPUT, SchemaDTO.class);

		// 获取证据信息
		String evidence = StateUtil.getStringValue(state, EVIDENCE);

		String multiTurn = StateUtil.getStringValue(state, MULTI_TURN_CONTEXT, "(无)");

		// 构建可行性评估提示词
		String prompt = PromptHelper.buildFeasibilityAssessmentPrompt(canonicalQuery, recalledSchema, evidence,
				multiTurn);
		log.debug("Built feasibility assessment prompt as follows \n {} \n", prompt);

		// 调用LLM进行可行性评估
		Flux<ChatResponse> responseFlux = llmService.callUser(prompt);

		Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGeneratorWithMessages(this.getChName(), this.getClass(),
				state, "正在进行可行性评估...", "可行性评估完成！", llmOutput -> {
					// 获取评估结果
					String assessmentResult = llmOutput.trim();
					log.info("Feasibility assessment result: {}", assessmentResult);
					// 返回评估结果
					return Map.of(FEASIBILITY_ASSESSMENT_NODE_OUTPUT, assessmentResult);
				}, responseFlux);
		return Map.of(FEASIBILITY_ASSESSMENT_NODE_OUTPUT, generator);
	}

}
