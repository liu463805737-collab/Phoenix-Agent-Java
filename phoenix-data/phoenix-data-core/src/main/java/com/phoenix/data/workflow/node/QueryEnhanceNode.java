package com.phoenix.data.workflow.node;

import com.phoenix.data.dto.prompt.QueryEnhanceOutputDTO;
import com.phoenix.data.enums.TextType;
import com.phoenix.data.prompt.PromptHelper;
import com.phoenix.data.service.llm.LlmService;
import com.phoenix.data.util.*;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.phoenix.data.utils.FluxUtil;
import com.phoenix.data.utils.JsonParseUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

import static com.phoenix.data.constant.Constant.*;

/**
 * 查询丰富节点，用于根据evidence信息把业务翻译。查询改写，扩展。 此节点不需要提取关键词，如果混合检索，如es等库会自行分词并计算相关性。
 */
@Slf4j
@Component
@AllArgsConstructor
public class QueryEnhanceNode extends AabstractNodeAction{

	private final LlmService llmService;

	private final JsonParseUtil jsonParseUtil;

	@Override
	public String getChName() {
		return "业务翻译";
	}

	/**
	 * 执行查询增强：结合证据信息改写和扩展用户查询。
	 *
	 * @param state 全局状态
	 * @return 包含增强后查询结果的结果映射
	 */
	@Override
	public Map<String, Object> apply(OverAllState state) throws Exception {

		// 获取用户输入
		String userInput = StateUtil.getStringValue(state, INPUT_KEY);
		log.info("User input for query enhance: {}", userInput);

		String evidence = StateUtil.getStringValue(state, EVIDENCE);
		String multiTurn = StateUtil.getStringValue(state, MULTI_TURN_CONTEXT, "(无)");

		// 构建查询处理提示
		String prompt = PromptHelper.buildQueryEnhancePrompt(multiTurn, userInput, evidence);
		log.debug("Built query enhance prompt as follows \n {} \n", prompt);

		// 调用LLM进行查询处理
		Flux<ChatResponse> responseFlux = llmService.callUser(prompt);

		Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGenerator(this.getChName(), this.getClass(), state,
				responseFlux,
				Flux.just(ChatResponseUtil.createResponse("正在进行问题增强..."),
						ChatResponseUtil.createPureResponse(TextType.JSON.getStartSign())),
				Flux.just(ChatResponseUtil.createPureResponse(TextType.JSON.getEndSign()),
						ChatResponseUtil.createResponse("\n问题增强完成！")),
				this::handleQueryEnhance);

		return Map.of(QUERY_ENHANCE_NODE_OUTPUT, generator);
	}

	/**
	 * 处理查询增强的 LLM 输出，解析为结构化对象。
	 *
	 * @param llmOutput LLM 原始输出
	 * @return 包含解析后查询增强结果的结果映射
	 */
	private Map<String, Object> handleQueryEnhance(String llmOutput) {
		// 获取处理结果
		String enhanceResult = MarkdownParserUtil.extractRawText(llmOutput.trim());
		log.info("Query enhance result: {}", enhanceResult);

		// 解析处理结果，转成 QueryProcessOutputDTO
		QueryEnhanceOutputDTO queryEnhanceOutputDTO = null;
		try {
			queryEnhanceOutputDTO = jsonParseUtil.tryConvertToObject(enhanceResult, QueryEnhanceOutputDTO.class);
			log.info("Successfully parsed query enhance result: {}", queryEnhanceOutputDTO);
		}
		catch (Exception e) {
			log.error("Failed to parse query enhance result: {}", enhanceResult, e);
		}

		if (queryEnhanceOutputDTO == null)
			return Map.of();
		// 返回处理结果
		return Map.of(QUERY_ENHANCE_NODE_OUTPUT, queryEnhanceOutputDTO);
	}

}
