package com.phoenix.data.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.phoenix.data.prompt.PromptConstant;
import com.phoenix.data.util.JsonUtil;
import com.phoenix.data.util.MarkdownParserUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import com.phoenix.data.service.llm.LlmService;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
/**
 * JSON解析工具类，支持自动修复格式错误的JSON
 */
@Slf4j
@Component
@AllArgsConstructor
public class JsonParseUtil {
	private LlmService llmService;

	private static final int MAX_RETRY_COUNT = 3;

	private static final String THINK_END_TAG = "</think>";

	/**
	 * 尝试将JSON字符串转换为指定类型的对象，支持自动修复格式错误的JSON
	 * @param json JSON字符串
	 * @param clazz 目标类型
	 * @return 转换后的对象
	 */
	public <T> T tryConvertToObject(String json, Class<T> clazz) {
		Assert.hasText(json, "Input JSON string cannot be null or empty");
		Assert.notNull(clazz, "Target class cannot be null");

		return tryConvertToObjectInternal(json, (mapper, currentJson) -> mapper.readValue(currentJson, clazz));
	}

	/**
	 * 尝试将JSON字符串转换为指定类型，支持TypeReference（如List<String>等复杂类型）
	 * @param json JSON字符串
	 * @param typeReference 类型引用
	 * @return 转换后的对象
	 */
	public <T> T tryConvertToObject(String json, TypeReference<T> typeReference) {
		Assert.hasText(json, "Input JSON string cannot be null or empty");
		Assert.notNull(typeReference, "TypeReference cannot be null");

		return tryConvertToObjectInternal(json, (mapper, currentJson) -> mapper.readValue(currentJson, typeReference));
	}

	/**
	 * 内部通用方法，用于JSON解析和修复
	 * @param json JSON字符串
	 * @param parser 解析器函数
	 * @return 转换后的对象
	 */
	private <T> T tryConvertToObjectInternal(String json, JsonParserFunction<T> parser) {
		log.info("Trying to convert JSON to object: {}", json);
		String currentJson = removeThinkTags(json);
		Exception lastException = null;
		ObjectMapper objectMapper = JsonUtil.getObjectMapper();

		try {
			return parser.parse(objectMapper, currentJson);
		}
		catch (JsonProcessingException e) {
			log.warn("Initial parsing failed, preparing to call LLM: {}", e.getMessage());
		}

		for (int i = 0; i < MAX_RETRY_COUNT; i++) {
			try {
				currentJson = callLlmToFix(currentJson,
						lastException != null ? lastException.getMessage() : "Unknown error");

				return parser.parse(objectMapper, currentJson);
			}
			catch (JsonProcessingException e) {
				lastException = e;
				log.warn("Still failed after {} fix attempt: {}", i + 1, e.getMessage());

				if (i == MAX_RETRY_COUNT - 1) {
					log.error("Finally failed after {} fix attempts", MAX_RETRY_COUNT);
					log.warn("Last fix result: {}", currentJson);
				}
			}
		}

		throw new IllegalArgumentException(
				String.format("Failed to parse JSON after %d LLM fix attempts", MAX_RETRY_COUNT), lastException);
	}

	/**
	 * 函数式接口，用于JSON解析
	 */
	@FunctionalInterface
	private interface JsonParserFunction<T> {

		T parse(ObjectMapper mapper, String json) throws JsonProcessingException;

	}

	/**
	 * 调用LLM修复格式错误的JSON
	 * @param json 原始JSON字符串
	 * @param errorMessage 解析错误信息
	 * @return 修复后的JSON字符串
	 */
	private String callLlmToFix(String json, String errorMessage) {
		try {
			String prompt = PromptConstant.getJsonFixPromptTemplate()
				.render(Map.of("json_string", json, "error_message", errorMessage));

			Flux<ChatResponse> responseFlux = llmService.callUser(prompt);
			String fixedJson = llmService.toStringFlux(responseFlux)
				.collect(StringBuilder::new, StringBuilder::append)
				.map(StringBuilder::toString)
				.block();

			// 检查fixedJson是否为null
			if (fixedJson == null) {
				log.warn("LLM fix returned null, using original JSON");
				return json;
			}

			log.debug("LLM original return content: {}", fixedJson);

			// 移除think标签
			String cleanedJson = removeThinkTags(fixedJson);
			log.debug("Content after removing think tags: {}", cleanedJson);

			// 提取可能输出在Markdown代码块中的内容
			cleanedJson = MarkdownParserUtil.extractRawText(cleanedJson);
			log.debug("Content after extracting Markdown code blocks: {}", cleanedJson);

			// 确保返回的JSON不为null
			return cleanedJson != null ? cleanedJson : json;
		}
		catch (Exception e) {
			log.error("Exception occurred while calling LLM fix service", e);
			return json;
		}
	}

	/**
	 * 移除 </think> 标签及其之前的所有内容 逻辑：找到最后一个 </think> 结束标签，只保留它之后的部分
	 */
	private String removeThinkTags(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}

		// 1. 查找最后一个结束标签的位置
		int lastEndTagIndex = text.lastIndexOf(THINK_END_TAG);

		if (lastEndTagIndex != -1) {
			log.debug("Found </think> tag, index position: {}", lastEndTagIndex);

			// 2. 计算截取点：结束标签的位置 + 标签本身的长度
			int contentStartIndex = lastEndTagIndex + THINK_END_TAG.length();

			// 3. 截取该点之后的所有内容
			String finalResult = text.substring(contentStartIndex).trim();

			log.debug("Content after truncating think tags: {}", finalResult);

			return finalResult;
		}

		// 如果没找到结束标签，说明可能没有思考过程，直接返回原文本（去除首尾空格）
		log.debug("Think end tag not found, returning original text");
		return text.trim();
	}

}
