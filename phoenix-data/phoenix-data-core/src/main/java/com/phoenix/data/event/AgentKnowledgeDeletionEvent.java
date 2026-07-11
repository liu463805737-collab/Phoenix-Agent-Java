package com.phoenix.data.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

/**
 * 智能体知识删除事件
 */
@Getter
public class AgentKnowledgeDeletionEvent extends ApplicationEvent {

	private final Integer knowledgeId;

	/**
	 * 构造知识删除事件
	 *
	 * @param source 事件源
	 * @param knowledgeId 知识ID
	 */
	public AgentKnowledgeDeletionEvent(Object source, Integer knowledgeId) {
		super(source, Clock.systemDefaultZone());
		this.knowledgeId = knowledgeId;
	}

}
