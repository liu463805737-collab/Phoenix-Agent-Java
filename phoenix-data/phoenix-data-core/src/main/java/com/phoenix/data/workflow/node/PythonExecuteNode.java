package com.phoenix.data.workflow.node;

import com.phoenix.data.enums.TextType;
import com.phoenix.data.properties.CodeExecutorProperties;
import com.phoenix.data.service.code.CodePoolExecutorService;
import com.phoenix.data.util.*;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.phoenix.data.utils.FluxUtil;
import com.phoenix.data.utils.JsonParseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.phoenix.data.constant.Constant.*;

/**
 * 根据SQL查询结果生成Python代码，并运行Python代码获取运行结果。
 *
 * @author vlsmb
 * @since 2025/7/29
 */
@Slf4j
@Component
public class PythonExecuteNode extends AabstractNodeAction {

	private final CodePoolExecutorService codePoolExecutor;

	private final ObjectMapper objectMapper;

	private final JsonParseUtil jsonParseUtil;

	private final CodeExecutorProperties codeExecutorProperties;

	@Override
	public String getChName() {
		return "生成Python并执行";
	}

	/**
	 * 构造 Python 执行节点。
	 *
	 * @param codePoolExecutor 代码池执行器
	 * @param jsonParseUtil JSON 解析工具
	 * @param codeExecutorProperties 代码执行器配置
	 */
	public PythonExecuteNode(CodePoolExecutorService codePoolExecutor, JsonParseUtil jsonParseUtil,
			CodeExecutorProperties codeExecutorProperties) {
		this.codePoolExecutor = codePoolExecutor;
		this.objectMapper = JsonUtil.getObjectMapper();
		this.jsonParseUtil = jsonParseUtil;
		this.codeExecutorProperties = codeExecutorProperties;
	}

	/**
	 * 执行 Python 代码：从状态中获取代码和 SQL 结果，运行并返回执行结果。
	 *
	 * @param state 全局状态
	 * @return 包含执行结果的结果映射
	 */
	@Override
	public Map<String, Object> apply(OverAllState state) throws Exception {

		try {
			// Get context
			String pythonCode = StateUtil.getStringValue(state, PYTHON_GENERATE_NODE_OUTPUT);
			List<Map<String, String>> sqlResults = StateUtil.hasValue(state, SQL_RESULT_LIST_MEMORY)
					? StateUtil.getListValue(state, SQL_RESULT_LIST_MEMORY) : new ArrayList<>();

			// 检查重试次数
			int triesCount = StateUtil.getObjectValue(state, PYTHON_TRIES_COUNT, Integer.class, 0);

			// 校验生成的代码是否是真正的 Python 代码，避免续写产生的中文提示文本被当作代码执行
			if (isLikelyNonCode(pythonCode)) {
				throw new RuntimeException("生成的代码不是有效的 Python 代码，可能因截断或未正确生成: " + pythonCode);
			}

			CodePoolExecutorService.TaskRequest taskRequest = new CodePoolExecutorService.TaskRequest(pythonCode,
					objectMapper.writeValueAsString(sqlResults), null);

			// Run Python code
			CodePoolExecutorService.TaskResponse taskResponse = this.codePoolExecutor.runTask(taskRequest);
			if (!taskResponse.isSuccess()) {
				String errorMsg = "Python Execute Failed!\nStdOut: " + taskResponse.stdOut() + "\nStdErr: "
						+ taskResponse.stdErr() + "\nExceptionMsg: " + taskResponse.exceptionMsg();
				log.error(errorMsg);

				// 检查是否超过最大重试次数
				if (triesCount >= codeExecutorProperties.getPythonMaxTriesCount()) {
					log.error("Python执行失败且已超过最大重试次数（已尝试次数：{}），启动降级兜底逻辑。错误信息: {}", triesCount, errorMsg);

					String fallbackOutput = "{}";

					Flux<ChatResponse> fallbackDisplayFlux = Flux.create(emitter -> {
						emitter.next(ChatResponseUtil.createResponse("开始执行Python代码..."));
						emitter.next(ChatResponseUtil.createResponse("Python代码执行失败已超过最大重试次数，采用降级策略继续处理。"));
						emitter.complete();
					});

					Flux<GraphResponse<StreamingOutput>> fallbackGenerator = FluxUtil
						.createStreamingGeneratorWithMessages(this.getChName(), this.getClass(), state,
								v -> Map.of(PYTHON_EXECUTE_NODE_OUTPUT, fallbackOutput, PYTHON_IS_SUCCESS, false,
										PYTHON_FALLBACK_MODE, true),
								fallbackDisplayFlux);

					return Map.of(PYTHON_EXECUTE_NODE_OUTPUT, fallbackGenerator);
				}

				throw new RuntimeException(errorMsg);
			}

			// Python输出必须是合法JSON，否则判定代码生成失败（如占位/说明文本），触发重试或降级
			String stdout = taskResponse.stdOut();
			String finalStdout;
			if (!StringUtils.hasText(stdout)) {
				throw new RuntimeException("Python执行结果为空，代码可能未正确输出数据");
			}
			try {
				// 先用廉价解析校验JSON合法性，占位/说明文本直接判定失败，避免浪费LLM修复
				objectMapper.readTree(stdout);
				Object value = jsonParseUtil.tryConvertToObject(stdout, Object.class);
				finalStdout = value != null ? objectMapper.writeValueAsString(value) : stdout;
			}
			catch (Exception e) {
				throw new RuntimeException("Python输出不是合法JSON，判定代码生成失败: " + e.getMessage(), e);
			}

			log.info("Python Execute Success! StdOut: {}", finalStdout);

			// Create display flux for user experience only
			Flux<ChatResponse> displayFlux = Flux.create(emitter -> {
				emitter.next(ChatResponseUtil.createResponse("开始执行Python代码..."));
				emitter.next(ChatResponseUtil.createResponse("标准输出："));
				emitter.next(ChatResponseUtil.createPureResponse(TextType.JSON.getStartSign()));
				emitter.next(ChatResponseUtil.createResponse(finalStdout));
				emitter.next(ChatResponseUtil.createPureResponse(TextType.JSON.getEndSign()));
				emitter.next(ChatResponseUtil.createResponse("Python代码执行成功！"));
				emitter.complete();
			});

			// Create generator using utility class, returning pre-computed business logic
			// result
			Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGeneratorWithMessages(this.getChName(),
					this.getClass(), state,
					v -> Map.of(PYTHON_EXECUTE_NODE_OUTPUT, finalStdout, PYTHON_IS_SUCCESS, true), displayFlux);

			return Map.of(PYTHON_EXECUTE_NODE_OUTPUT, generator);
		}
		catch (Exception e) {
			String errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
			log.error("Python Execute Exception: {}", errorMessage);

			// 已超过最大重试次数时同样进入降级模式，避免流程被强制终止
			int currentTries = StateUtil.getObjectValue(state, PYTHON_TRIES_COUNT, Integer.class, 0);
			boolean fallbackMode = currentTries >= codeExecutorProperties.getPythonMaxTriesCount();

			// Prepare error result
			Map<String, Object> errorResult = Map.of(PYTHON_EXECUTE_NODE_OUTPUT, errorMessage, PYTHON_IS_SUCCESS,
					false, PYTHON_FALLBACK_MODE, fallbackMode);

			// Create error display flux
			Flux<ChatResponse> errorDisplayFlux = Flux.create(emitter -> {
				emitter.next(ChatResponseUtil.createResponse("开始执行Python代码..."));
				emitter.next(ChatResponseUtil.createResponse("Python代码执行失败: " + errorMessage));
				emitter.complete();
			});

			// Create error generator using utility class
			var generator = FluxUtil.createStreamingGeneratorWithMessages(this.getChName(), this.getClass(), state, v -> errorResult,
					errorDisplayFlux);

			return Map.of(PYTHON_EXECUTE_NODE_OUTPUT, generator);
		}
	}

	/**
	 * 判断生成的代码是否明显不是 Python 代码。
	 * 跳过空行和注释行后，若首个代码行以汉字开头，基本可断定是续写产生的中文提示文本而非代码。
	 *
	 * @param code 生成的代码
	 * @return 是否明显不是有效的 Python 代码
	 */
	private boolean isLikelyNonCode(String code) {
		if (code == null || code.isBlank()) {
			return true;
		}
		for (String line : code.split("\n")) {
			String trimmed = line.stripLeading();
			if (trimmed.isEmpty() || trimmed.startsWith("#")) {
				continue;
			}
			if (!trimmed.isEmpty() && Character.UnicodeScript.of(trimmed.codePointAt(0)) == Character.UnicodeScript.HAN) {
				return true;
			}
			break;
		}
		return false;
	}

}
