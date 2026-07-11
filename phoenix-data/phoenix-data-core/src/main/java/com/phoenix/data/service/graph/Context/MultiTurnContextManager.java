package com.phoenix.data.service.graph.Context;

import com.phoenix.data.properties.DataAgentProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages multi-turn dialogue context for each thread. The context keeps a lightweight
 * history of user questions and the corresponding planner outputs so downstream prompts
 * can reference prior turns.
 */
@Slf4j
@Component
@AllArgsConstructor
public class MultiTurnContextManager {

	/** 配置属性 */
	private final DataAgentProperties properties;

	/** 多轮对话历史记录（todo：考虑持久化存储） */
	private final Map<String, Deque<ConversationTurn>> history = new ConcurrentHashMap<>();

	/** 当前正在处理中的轮次 */
	private final Map<String, PendingTurn> pendingTurns = new ConcurrentHashMap<>();

	/**
	 * Start tracking a new turn for the given thread.
	 * @param threadId conversation thread id
	 * @param userQuestion latest user question
	 */
	public void beginTurn(String threadId, String userQuestion) {
		if (StringUtils.isAnyBlank(threadId, userQuestion)) {
			return;
		}
		pendingTurns.put(threadId, new PendingTurn(userQuestion.trim()));
	}

	/**
	 * Append planner output chunk for the current turn.
	 * @param threadId conversation thread id
	 * @param chunk planner streaming chunk
	 */
	public void appendPlannerChunk(String threadId, String chunk) {
		if (StringUtils.isAnyBlank(threadId, chunk)) {
			return;
		}
		PendingTurn pending = pendingTurns.get(threadId);
		if (pending != null) {
			pending.planBuilder.append(chunk);
		}
	}

	/**
	 * Finalize current turn and add to history if planner output is available.
	 * @param threadId conversation thread id
	 */
	public void finishTurn(String threadId) {
		PendingTurn pending = pendingTurns.remove(threadId);
		if (pending == null) {
			return;
		}
		String plan = StringUtils.trimToEmpty(pending.planBuilder.toString());
		if (StringUtils.isBlank(plan)) {
			log.debug("No planner output recorded for thread {}, skipping history update", threadId);
			return;
		}

		String trimmedPlan = StringUtils.abbreviate(plan, properties.getMaxplanlength());
		Deque<ConversationTurn> deque = history.computeIfAbsent(threadId, k -> new ArrayDeque<>());
		synchronized (deque) {
			while (deque.size() >= properties.getMaxturnhistory()) {
				deque.pollFirst();
			}
			deque.addLast(new ConversationTurn(pending.userQuestion, trimmedPlan));
		}
	}

	/**
	 * Remove any pending turn data without touching persisted history. Typically used
	 * when a run is aborted.
	 * @param threadId conversation thread id
	 */
	public void discardPending(String threadId) {
		pendingTurns.remove(threadId);
	}

	/**
	 * Restart the latest turn so a new planner output can replace it (e.g. after human
	 * feedback). The last stored turn will be removed and its question reused.
	 * @param threadId conversation thread id
	 */
	public void restartLastTurn(String threadId) {
		Deque<ConversationTurn> deque = history.get(threadId);
		if (deque == null || deque.isEmpty()) {
			return;
		}
		ConversationTurn lastTurn;
		synchronized (deque) {
			lastTurn = deque.pollLast();
		}
		if (lastTurn != null) {
			pendingTurns.put(threadId, new PendingTurn(lastTurn.userQuestion()));
		}
	}

	/**
	 * Build multi-turn context string for prompt injection.
	 * @param threadId conversation thread id
	 * @return formatted history string
	 */
	public String buildContext(String threadId) {
		Deque<ConversationTurn> deque = history.get(threadId);
		if (deque == null || deque.isEmpty()) {
			return "(无)";
		}
		return deque.stream()
			.map(turn -> "用户: " + turn.userQuestion() + "\nAI计划: " + turn.plan())
			.collect(Collectors.joining("\n"));
	}

	/**
	 * 对话轮次记录
	 * @param userQuestion 用户问题
	 * @param plan 智能体计划
	 */
	private record ConversationTurn(String userQuestion, String plan) {
	}

	/**
	 * 正在处理中的轮次
	 */
	private static class PendingTurn {

		/** 用户问题 */
		private final String userQuestion;

		/** 计划内容构建器 */
		private final StringBuilder planBuilder = new StringBuilder();

		/**
		 * 创建待处理轮次
		 * @param userQuestion 用户问题
		 */
		private PendingTurn(String userQuestion) {
			this.userQuestion = userQuestion;
		}

	}

}
