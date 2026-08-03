package com.phoenix.data.workflow.node;

import com.phoenix.data.dto.planner.ExecutionStep;
import com.phoenix.data.dto.schema.ColumnDTO;
import com.phoenix.data.dto.schema.SchemaDTO;
import com.phoenix.data.dto.schema.TableDTO;
import com.phoenix.data.enums.TextType;
import com.phoenix.data.prompt.PromptConstant;
import com.phoenix.data.properties.CodeExecutorProperties;
import com.phoenix.data.properties.DataAgentProperties;
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
	private static final int MAX_CONTINUATION_DEPTH = 5;

	private final ObjectMapper objectMapper;

	private final CodeExecutorProperties codeExecutorProperties;

	private final DataAgentProperties dataAgentProperties;

	private final LlmService llmService;

	@Override
	public String getChName() {
		return "生成Python";
	}

	/**
	 * 构造 Python 生成节点。
	 *
	 * @param codeExecutorProperties 代码执行器配置
	 * @param dataAgentProperties    数据智能体配置
	 * @param llmService LLM 服务
	 */
	public PythonGenerateNode(CodeExecutorProperties codeExecutorProperties, DataAgentProperties dataAgentProperties,
			LlmService llmService) {
		this.codeExecutorProperties = codeExecutorProperties;
		this.dataAgentProperties = dataAgentProperties;
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
					codeExecutorProperties.getCodeTimeout(), "database_schema", buildCompactSchema(schemaDTO),
					"sample_input",
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
	 * 递归生成：检测到 finish_reason=length 时丢弃被截断的代码，重新生成一份更精简的完整脚本，
	 * 避免拼接半截代码导致语法错误或占位文本。只输出最终完整一版的代码。
	 *
	 * @param systemPrompt 系统提示词
	 * @param userPrompt   用户提示词（分析需求）
	 * @param depth        当前尝试次数（0 为首次生成）
	 * @return 合并后的 LLM 响应流
	 */
	private Flux<ChatResponse> generateWithContinuation(String systemPrompt, String userPrompt, int depth) {
		if (depth >= MAX_CONTINUATION_DEPTH) {
			log.warn("Python generation reached max depth ({}), code may still be incomplete", MAX_CONTINUATION_DEPTH);
			return Flux.empty();
		}

		ChatResponse[] lastResponse = new ChatResponse[1];

		Flux<ChatResponse> currentCall = (depth == 0)
				? llmService.call(systemPrompt, userPrompt)
				: llmService.callUser(buildRegeneratePrompt(systemPrompt, userPrompt, depth),
						dataAgentProperties.getLlmMaxOutputTokens());
		currentCall = currentCall.doOnNext(r -> lastResponse[0] = r);

		return currentCall.collectList()
			.flatMapMany(roundChunks -> {
				if (isTruncated(lastResponse[0])) {
					log.warn("Python code truncated by token limit, regenerating shorter code (attempt {})",
							depth + 1);
					return generateWithContinuation(systemPrompt, userPrompt, depth + 1);
				}
				log.info("Python code generated successfully (attempt {})", depth);
				return Flux.fromIterable(roundChunks);
			});
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
	 * 构建重新生成的 prompt：携带完整的分析上下文与更精简的要求。
	 *
	 * @param systemPrompt 原始系统提示词（含表结构、样例、方案）
	 * @param userPrompt   用户分析需求
	 * @param attempt      第几次重新生成
	 * @return 重新生成 prompt
	 */
	private String buildRegeneratePrompt(String systemPrompt, String userPrompt, int attempt) {
		return "你之前生成的Python代码因为输出长度限制被截断（第" + attempt + "次重新生成）。\n"
				+ "请重新生成一份【精简】的完整 Python 分析脚本（目标 100 行以内），直接完成全部分析逻辑。\n"
				+ "要求：\n" + "1. 必须从 sys.stdin 读取输入数据：input_data = json.load(sys.stdin)；\n"
				+ "2. 最终通过 print(json.dumps(result, ensure_ascii=False)) 输出合法 JSON；\n"
				+ "3. 代码必须完整、可独立运行，不能是占位代码，不能只输出说明文字；\n"
				+ "4. 保持精简，避免再次因过长被截断。\n\n"
				+ systemPrompt + "\n\n【分析需求】\n" + userPrompt + "\n\n直接输出 Python 代码：";
	}

	/**
	 * 将 schema 压缩为精简的表结构描述（仅保留表名、列名和类型），
	 * 减小送入 LLM 的上下文体积，加快生成速度。
	 *
	 * @param schemaDTO 完整 schema
	 * @return 精简后的表结构文本
	 */
	private String buildCompactSchema(SchemaDTO schemaDTO) {
		if (schemaDTO == null || schemaDTO.getTable() == null || schemaDTO.getTable().isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (TableDTO table : schemaDTO.getTable()) {
			sb.append("CREATE TABLE ").append(table.getName()).append(" (\n");
			if (table.getColumn() != null) {
				for (ColumnDTO column : table.getColumn()) {
					sb.append("  `").append(column.getName()).append("`");
					if (column.getType() != null && !column.getType().isBlank()) {
						sb.append(" ").append(column.getType());
					}
					sb.append(",\n");
				}
			}
			sb.append(");\n");
		}
		return sb.toString();
	}

}
