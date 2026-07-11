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
import com.alibaba.cloud.ai.graph.action.NodeAction;
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
			// Last generated Python code failed to run, inform AI model of this
			// information
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
		Flux<ChatResponse> pythonGenerateFlux = llmService.call(systemPrompt, userPrompt);

		Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGeneratorWithMessages(this.getChName(), this.getClass(),
				state, aiResponse -> {
					// Some AI models still output Markdown markup (even though Prompt has
					// emphasized this)
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

}
