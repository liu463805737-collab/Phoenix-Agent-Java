package com.phoenix.data.prompt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prompt loader, used to load prompt templates from file system
 *
 * @author zhangshenghang
 */
@Slf4j
public class PromptLoader {

	private static final String PROMPT_PATH_PREFIX = "prompts/";

	private static final ConcurrentHashMap<String, String> promptCache = new ConcurrentHashMap<>();

	/**
	 * Load prompt template from file
	 * @param promptName prompt file name (without path and extension)
	 * @return prompt content
	 */
	public static String loadPrompt(String promptName) {
		return promptCache.computeIfAbsent(promptName, name -> {
			String fileName = PROMPT_PATH_PREFIX + name + ".txt";
			// 使用本类的类加载器获取资源（避免jar包中无法获取资源）
			try (InputStream inputStream = PromptLoader.class.getClassLoader().getResourceAsStream(fileName)) {
				return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
			}
			catch (IOException e) {
				log.error("加载提示词失败！{}", e.getMessage(), e);
				throw new RuntimeException("加载提示词失败: " + name, e);
			}
		});
	}

	/**
	 * Clear prompt cache
	 */
	public static void clearCache() {
		promptCache.clear();
	}

	/**
	 * Get cache size
	 * @return number of prompts in cache
	 */
	public static int getCacheSize() {
		return promptCache.size();
	}

}
