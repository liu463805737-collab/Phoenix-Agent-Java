package com.phoenix.data.workflow.node;

import com.phoenix.data.dto.planner.ExecutionStep;
import com.phoenix.data.dto.schema.SchemaDTO;
import com.phoenix.data.enums.TextType;
import com.phoenix.data.prompt.PromptConstant;
import com.phoenix.data.properties.CodeExecutorProperties;
import com.phoenix.data.service.llm.LlmService;
import com.phoenix.data.util.*;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenix.data.utils.FluxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.phoenix.data.constant.Constant.*;

/**
 * 生成Python代码的节点
 *
 * @author vlsmb
 * @since 2025/7/30
 */
@Slf4j
@Component
public class PythonGenerateNode extends AabstractNodeAction {

	/**
	 * 样本数据数量限制
	 */
	private static final int SAMPLE_DATA_NUMBER = 5;

	/**
	 * Python生成最大续写深度
	 */
	private static final int MAX_CONTINUATION_DEPTH = 3;

	private final ObjectMapper objectMapper;

	private final CodeExecutorProperties codeExecutorProperties;

	private final LlmService llmService;

	@Override
	public String getChName() {
		return "生成Python";
	}

	/**
	 * 构造 Python 生成节点。
	 *
	 * @param codeExecutorProperties 代码执行器配置
	 * @param llmService LLM 服务
	 */
	public PythonGenerateNode(CodeExecutorProperties codeExecutorProperties, LlmService llmService) {
		this.codeExecutorProperties = codeExecutorProperties;
		this.llmService = llmService;
		this.objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	/**
	 * 执行 Python 代码生成：根据 Schema、SQL 结果和计划步骤生成 Python 代码。
	 *
	 * @param state 全局状态
	 * @return 包含生成代码的结果映射
	 */
	@Override
	public Map<String, Object> apply(OverAllState state) throws Exception {

		// Get context
		SchemaDTO schemaDTO = StateUtil.getObjectValue(state, TABLE_RELATION_OUTPUT, SchemaDTO.class);
		List<Map<String, String>> sqlResults = StateUtil.hasValue(state, SQL_RESULT_LIST_MEMORY)
				? StateUtil.getListValue(state, SQL_RESULT_LIST_MEMORY) : new ArrayList<>();
		boolean codeRunSuccess = StateUtil.getObjectValue(state, PYTHON_IS_SUCCESS, Boolean.class, true);
		int triesCount = StateUtil.getObjectValue(state, PYTHON_TRIES_COUNT, Integer.class, 0);

		String userPrompt = StateUtil.getCanonicalQuery(state);
		if (!codeRunSuccess) {
			String lastCode = StateUtil.getStringValue(state, PYTHON_GENERATE_NODE_OUTPUT);
			String lastError = StateUtil.getStringValue(state, PYTHON_EXECUTE_NODE_OUTPUT);
			userPrompt += String.format("""
					上次尝试生成的Python代码运行失败，请你重新生成符合要求的Python代码。
					【上次生成代码】
					```python
					%s
					```
					【运行错误信息】
					```
					%s
					```
					""", lastCode, lastError);
		}

		ExecutionStep executionStep = PlanProcessUtil.getCurrentExecutionStep(state);

		ExecutionStep.ToolParameters toolParameters = executionStep.getToolParameters();

		// Load Python code generation template
		String systemPrompt = PromptConstant.getPythonGeneratorPromptTemplate()
			.render(Map.of("python_memory", codeExecutorProperties.getLimitMemory().toString(), "python_timeout",
					codeExecutorProperties.getCodeTimeout(), "database_schema",
					objectMapper.writeValueAsString(schemaDTO), "sample_input",
					objectMapper.writeValueAsString(sqlResults.stream().limit(SAMPLE_DATA_NUMBER).toList()),
					"plan_description", objectMapper.writeValueAsString(toolParameters)));

		// 递归续写：检测到 finish_reason=length 时自动续写，直到完整或达到最大深度
		Flux<ChatResponse> pythonGenerateFlux = generateWithContinuation(systemPrompt, userPrompt, 0);

		Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGeneratorWithMessages(this.getChName(), this.getClass(),
				state, aiResponse -> {
					aiResponse = aiResponse.substring(TextType.PYTHON.getStartSign().length(),
							aiResponse.length() - TextType.PYTHON.getEndSign().length());
					aiResponse = MarkdownParserUtil.extractRawText(aiResponse);
					log.info("Python Generate Code: {}", aiResponse);
					return Map.of(PYTHON_GENERATE_NODE_OUTPUT, aiResponse, PYTHON_TRIES_COUNT, triesCount + 1);
				},
				Flux.concat(Flux.just(ChatResponseUtil.createPureResponse(TextType.PYTHON.getStartSign())),
						pythonGenerateFlux,
						Flux.just(ChatResponseUtil.createPureResponse(TextType.PYTHON.getEndSign()))));

		return Map.of(PYTHON_GENERATE_NODE_OUTPUT, generator);
	}

	/**
	 * 递归续写：检测到 finish_reason=length 时自动续写，直到完整或达到最大深度。
	 *
	 * @param systemPrompt 系统提示词
	 * @param userPrompt   用户提示词
	 * @param depth        当前递归深度
	 * @return 合并后的 LLM 响应流
	 */
	private Flux<ChatResponse> generateWithContinuation(String systemPrompt, String userPrompt, int depth) {
		if (depth >= MAX_CONTINUATION_DEPTH) {
			log.warn("Python generation continuation reached max depth ({}), code may still be incomplete", MAX_CONTINUATION_DEPTH);
			return Flux.empty();
		}

		StringBuilder accumulated = new StringBuilder();
		ChatResponse[] lastResponse = new ChatResponse[1];

		Flux<ChatResponse> currentCall = llmService.call(systemPrompt, userPrompt)
			.doOnNext(r -> {
				lastResponse[0] = r;
				accumulated.append(ChatResponseUtil.getText(r));
			});

		return Flux.concat(
				currentCall,
				Flux.defer(() -> {
					if (lastResponse[0] != null
							&& lastResponse[0].getResult() != null
							&& lastResponse[0].getResult().getMetadata() != null
							&& "LENGTH".equals(lastResponse[0].getResult().getMetadata().getFinishReason())) {
						log.warn("Python code truncated by token limit, initiating continuation (depth={})", depth + 1);
						String continuationPrompt = buildContinuationPrompt(accumulated.toString(), depth + 1);
						return generateWithContinuation(systemPrompt, continuationPrompt, depth + 1);
					}
					return Flux.empty();
				}));
	}

	/**
	 * 构建续写 prompt，携带完整已输出代码作为上下文。
	 *
	 * @param accumulatedCode   当前轮已输出的代码
	 * @param continuationCount 第几次续写
	 * @return 续写 prompt
	 */
	private String buildContinuationPrompt(String accumulatedCode, int continuationCount) {
		return String.format("""
				你之前生成的Python代码因为长度限制被截断（第%d次续写）。
				请直接从断点处继续生成，只输出剩余的代码，**不要重复**已经输出的内容。
				请严格遵循原始要求中的代码规范。

				【已输出的代码（请继续，不要重复）】
				```python
				%s
				```

				请从上面代码的断点处继续：
				""", continuationCount, accumulatedCode);
	}

}
