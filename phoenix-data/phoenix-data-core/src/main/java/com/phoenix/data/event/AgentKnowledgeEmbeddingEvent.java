package com.phoenix.data.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

/**
 * 智能体知识向量化事件
 */
@Getter
public class AgentKnowledgeEmbeddingEvent extends ApplicationEvent {

	private final Integer knowledgeId;

	private final String splitterType;

	/**
	 * 构造知识向量化事件
	 *
	 * @param source 事件源
	 * @param knowledgeId 知识ID
	 * @param splitterType 分块策略类型
	 */
	public AgentKnowledgeEmbeddingEvent(Object source, Integer knowledgeId, String splitterType) {
		super(source, Clock.systemDefaultZone());
		this.knowledgeId = knowledgeId;
		this.splitterType = splitterType;
	}

}
