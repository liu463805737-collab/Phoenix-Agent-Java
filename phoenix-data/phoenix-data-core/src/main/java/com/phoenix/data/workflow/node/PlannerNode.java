package com.phoenix.data.workflow.node;

import com.phoenix.data.dto.planner.Plan;
import com.phoenix.data.dto.schema.SchemaDTO;
import com.phoenix.data.enums.TextType;
import com.phoenix.data.prompt.PromptConstant;
import com.phoenix.data.prompt.PromptHelper;
import com.phoenix.data.service.llm.LlmService;
import com.phoenix.data.util.ChatResponseUtil;
import com.phoenix.data.utils.FluxUtil;
import com.phoenix.data.util.StateUtil;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

import static com.phoenix.data.constant.Constant.*;

/**
 * @author zhangshenghang
 */
@Slf4j
@Component
@AllArgsConstructor
public class PlannerNode extends AabstractNodeAction {

	private static final int MAX_CONTINUATION_DEPTH = 5;

	private final LlmService llmService;

	@Override
	public String getChName() {
		return "执行计划";
	}

	/**
	 * 执行计划生成：根据 NL2SQL 模式或完整模式生成执行计划。
	 *
	 * @param state 全局状态
	 * @return 包含计划输出的结果映射
	 */
	@Override
	public Map<String, Object> apply(OverAllState state) throws Exception {
		// 是否为NL2SQL模式
		Boolean onlyNl2sql = state.value(IS_ONLY_NL2SQL, false);

		Flux<ChatResponse> flux = onlyNl2sql ? handleNl2SqlOnly() : handlePlanGenerate(state);

		Flux<ChatResponse> chatResponseFlux = Flux.concat(
				Flux.just(ChatResponseUtil.createPureResponse(TextType.JSON.getStartSign())), flux,
				Flux.just(ChatResponseUtil.createPureResponse(TextType.JSON.getEndSign())));
		Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGeneratorWithMessages(this.getChName(), this.getClass(),
				state, v -> Map.of(PLANNER_NODE_OUTPUT, v.substring(TextType.JSON.getStartSign().length(),
						v.length() - TextType.JSON.getEndSign().length())),
				chatResponseFlux);

		return Map.of(PLANNER_NODE_OUTPUT, generator);
	}

	/**
	 * 处理完整模式的计划生成，结合 Schema、证据和语义模型。
	 *
	 * @param state 全局状态
	 * @return LLM 响应流
	 */
	private Flux<ChatResponse> handlePlanGenerate(OverAllState state) {
		// 获取查询增强节点的输出
		String canonicalQuery = StateUtil.getCanonicalQuery(state);
		log.info("Using processed query for planning: {}", canonicalQuery);

		// 检查是否为修复模式
		String validationError = StateUtil.getStringValue(state, PLAN_VALIDATION_ERROR, null);
		if (validationError != null) {
			log.info("Regenerating plan with user feedback: {}", validationError);
		}
		else {
			log.info("Generating initial plan");
		}

		// 构建提示参数
		String semanticModel = (String) state.value(GENEGRATED_SEMANTIC_MODEL_PROMPT).orElse("");
		SchemaDTO schemaDTO = StateUtil.getObjectValue(state, TABLE_RELATION_OUTPUT, SchemaDTO.class);
		String schemaStr = PromptHelper.buildMixMacSqlDbPrompt(schemaDTO, true);

		// 构建用户提示
		String userPrompt = buildUserPrompt(canonicalQuery, validationError, state);
		String evidence = StateUtil.getStringValue(state, EVIDENCE);

		// 构建模板参数
		BeanOutputConverter<Plan> beanOutputConverter = new BeanOutputConverter<>(Plan.class);
		Map<String, Object> params = Map.of("user_question", userPrompt, "schema", schemaStr, "evidence", evidence,
				"semantic_model", semanticModel, "plan_validation_error", formatValidationError(validationError),
				"format", beanOutputConverter.getFormat());
		// 生成计划
		String plannerPrompt = PromptConstant.getPlannerPromptTemplate().render(params);
		log.debug("Planner prompt: as follows \n{}\n", plannerPrompt);

		// 递归续写：检测到 finish_reason=length 时自动续写，直到计划完整或达到最大深度
		return callWithContinuation(plannerPrompt, 0);
	}

	/**
	 * 递归续写：检测到 finish_reason=length 时自动续写，直到计划完整或达到最大深度。
	 *
	 * @param prompt LLM 调用 prompt
	 * @param depth 当前递归深度
	 * @return 合并后的 LLM 响应流
	 */
	private Flux<ChatResponse> callWithContinuation(String prompt, int depth) {
		if (depth >= MAX_CONTINUATION_DEPTH) {
			log.warn("Plan continuation reached max depth ({}), plan may still be incomplete", MAX_CONTINUATION_DEPTH);
			return Flux.empty();
		}

		StringBuilder accumulated = new StringBuilder();
		ChatResponse[] lastResponse = new ChatResponse[1];

		Flux<ChatResponse> currentCall = llmService.callUser(prompt)
			.doOnNext(r -> {
				lastResponse[0] = r;
				accumulated.append(ChatResponseUtil.getText(r));
			});

		return Flux.concat(currentCall,
				Flux.defer(() -> {
					if (isTruncated(lastResponse[0])) {
						log.warn("Plan truncated by token limit, initiating continuation (depth={})", depth + 1);
						return callWithContinuation(buildContinuationPrompt(accumulated.toString(), depth + 1),
								depth + 1);
					}
					return Flux.empty();
				}));
	}

	/**
	 * 判断 LLM 响应是否因 token 上限被截断。
	 *
	 * @param response 最后一条响应
	 * @return 是否被截断
	 */
	private boolean isTruncated(ChatResponse response) {
		return response != null && response.getResult() != null && response.getResult().getMetadata() != null
				&& "LENGTH".equals(response.getResult().getMetadata().getFinishReason());
	}

	/**
	 * 构建续写 prompt，携带最后一段已输出内容作为上下文。
	 *
	 * @param lastSegment 当前轮已输出的内容
	 * @param continuationCount 第几次续写
	 * @return 续写 prompt
	 */
	private String buildContinuationPrompt(String lastSegment, int continuationCount) {
		String tail = (lastSegment != null && lastSegment.length() > 500)
				? lastSegment.substring(lastSegment.length() - 500) : lastSegment;
		return "执行计划 JSON 因长度限制被截断（第" + continuationCount
				+ "次续写），请直接从断点处继续生成 JSON，不要重复已经输出的内容，也不要输出多余的解释或 markdown 标记。\n\n"
				+ "已输出的最后内容（仅供参考，不要重复）：\n" + tail + "\n\n请继续：";
	}

	/**
	 * 处理仅 NL2SQL 模式，直接返回预设计划。
	 *
	 * @return LLM 响应流
	 */
	private Flux<ChatResponse> handleNl2SqlOnly() {
		return Flux.just(ChatResponseUtil.createPureResponse(Plan.nl2SqlPlan()));
	}

	/**
	 * 构建用户提示词，包含校验错误信息（如有）。
	 *
	 * @param input 用户原始输入
	 * @param validationError 校验错误信息
	 * @param state 全局状态
	 * @return 构建后的提示词
	 */
	private String buildUserPrompt(String input, String validationError, OverAllState state) {
		if (validationError == null) {
			return input;
		}

		String previousPlan = StateUtil.getStringValue(state, PLANNER_NODE_OUTPUT, "");
		return String.format(
				"IMPORTANT: User rejected previous plan with feedback: \"%s\"\n\n" + "Original question: %s\n\n"
						+ "Previous rejected plan:\n%s\n\n"
						+ "CRITICAL: Generate new plan incorporating user feedback (\"%s\")",
				validationError, input, previousPlan, validationError);
	}

	/**
	 * 格式化校验错误信息，添加标记以便 LLM 理解。
	 *
	 * @param validationError 原始错误信息
	 * @return 格式化后的错误信息
	 */
	private String formatValidationError(String validationError) {
		return validationError != null ? String
			.format("**USER FEEDBACK (CRITICAL)**: %s\n\n**Must incorporate this feedback.**", validationError) : "";
	}

}
