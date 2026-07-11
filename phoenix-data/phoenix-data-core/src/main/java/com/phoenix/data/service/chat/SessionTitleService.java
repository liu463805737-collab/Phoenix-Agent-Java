package com.phoenix.data.service.chat;

import com.phoenix.data.entity.ChatSession;
import com.phoenix.data.service.llm.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * 会话标题服务，通过 LLM 异步生成会话标题并推送到前端。
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class SessionTitleService {

	private static final String DEFAULT_TITLE = "新会话";

	private final ChatSessionService chatSessionService;

	private final SessionEventPublisher sessionEventPublisher;

	private final LlmService llmService;

	@Qualifier("dbOperationExecutor")
	private final ExecutorService executorService;

	private final Set<String> runningTasks = ConcurrentHashMap.newKeySet();

	/**
	 * 安排异步生成会话标题（去重，避免重复任务）
	 */
	public void scheduleTitleGeneration(String sessionId, String userMessage) {
		if (!StringUtils.hasText(sessionId) || !StringUtils.hasText(userMessage)) {
			return;
		}
		if (!runningTasks.add(sessionId)) {
			return;
		}
		CompletableFuture.runAsync(() -> generateAndPersist(sessionId, userMessage), executorService)
			.whenComplete((unused, throwable) -> runningTasks.remove(sessionId));
	}

	/**
	 * 生成标题并持久化到数据库，同时推送更新事件
	 */
	private void generateAndPersist(String sessionId, String userMessage) {
		try {
			ChatSession session = chatSessionService.findBySessionId(sessionId);
			if (session == null) {
				log.warn("Session {} not found when generating title", sessionId);
				return;
			}
			if (hasCustomTitle(session)) {
				log.debug("Session {} already has custom title, skip generating", sessionId);
				return;
			}

			String title = requestSummary(userMessage);
			if (!StringUtils.hasText(title)) {
				title = fallbackTitle(userMessage);
			}
			title = normalizeTitle(title);
			if (!StringUtils.hasText(title)) {
				log.warn("LLM returned empty title for session {}", sessionId);
				return;
			}

			chatSessionService.renameSession(sessionId, title);
			sessionEventPublisher.publishTitleUpdated(session.getAgentId(), sessionId, title);
			log.info("Generated session title '{}' for session {}", title, sessionId);
		}
		catch (Exception ex) {
			log.error("Failed to generate session title for session {}: {}", sessionId, ex.getMessage());
		}
	}

	/**
	 * 检查会话是否已设置自定义标题
	 */
	private boolean hasCustomTitle(ChatSession session) {
		return StringUtils.hasText(session.getTitle()) && !DEFAULT_TITLE.equals(session.getTitle());
	}

	/**
	 * 调用 LLM 请求生成摘要标题
	 */
	private String requestSummary(String userMessage) {
		try {
			String systemPrompt = """
					你是一名对话助手，请根据用户的第一条输入生成不超过20个字的会话标题。
					使用中文输出，避免使用标点或引号，仅保留核心主题。
					""";
			String userPrompt = "用户输入：" + userMessage;
			Flux<String> responseFlux = llmService.toStringFlux(llmService.call(systemPrompt, userPrompt));
			return responseFlux.collect(StringBuilder::new, StringBuilder::append)
				.map(StringBuilder::toString)
				.block(Duration.ofSeconds(15));
		}
		catch (Exception ex) {
			log.warn("LLM title generation failed: {}", ex.getMessage());
			return null;
		}
	}

	/**
	 * 规范化标题（去除换行和引号，限制长度不超过 20 字）
	 */
	private String normalizeTitle(String raw) {
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		String sanitized = raw.replaceAll("[\\r\\n]+", " ").replaceAll("[\"“”]+", "").trim();
		if (sanitized.length() > 20) {
			sanitized = sanitized.substring(0, 20);
		}
		return sanitized;
	}

	/**
	 * 生成回退标题（取用户消息的前 20 字）
	 */
	private String fallbackTitle(String userMessage) {
		String text = userMessage.replaceAll("\\s+", " ").trim();
		if (text.length() > 20) {
			text = text.substring(0, 20);
		}
		return StringUtils.hasText(text) ? text : DEFAULT_TITLE;
	}

}
