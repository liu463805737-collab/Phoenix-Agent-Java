package com.phoenix.data.service.knowledge;

import com.phoenix.data.enums.SplitterType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.stereotype.Component;

import java.util.Map;

// TODO 后续需改造 AgentKnowledgeResourceManager 使用该类获取对应的 TextSplitter，然后前端提供页面让用户选择不同的切割方式
/**
 * 文本分割器工厂，根据类型获取对应的文本分割器实例
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TextSplitterFactory {

	/** 分割器Bean映射表 */
	private final Map<String, TextSplitter> splitterMap;

	/**
	 * 根据类型字符串获取对应的 Splitter
	 * @param type 前端传入的类型，例如 "token", "recursive"
	 * @return 对应的 TextSplitter 实例
	 */
	public TextSplitter getSplitter(String type) {
		// 1. 尝试直接获取
		TextSplitter splitter = splitterMap.get(type);

		// 2. 如果没找到，尝返回默认
		if (splitter == null) {
			log.warn("Splitter type '{}' not found, falling back to default 'token'", type);
			return splitterMap.get(SplitterType.TOKEN.getValue());
		}

		return splitter;
	}

}
