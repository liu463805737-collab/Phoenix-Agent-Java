package com.phoenix.data.service.chat;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.data.entity.ChatSession;
import com.phoenix.data.mapper.ChatSessionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 聊天会话服务实现类，提供会话的创建、查询、软删除及状态更新操作。
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession>
		implements ChatSessionService {

	@Override
	@Transactional(readOnly = true)
	public List<ChatSession> findByAgentIdAndUserId(Integer agentId, String userId) {
		return QueryChain.of(getMapper())
			.eq(ChatSession::getAgentId, agentId)
			.eq(ChatSession::getUserId, userId)
			.ne(ChatSession::getStatus, "deleted")
			.orderBy(ChatSession::getIsPinned, false)
			.orderBy(ChatSession::getUpdateTime, false)
			.list();
	}

	/**
	 * 根据 Agent ID 获取会话列表
	 */
	@Override
	public List<ChatSession> findByAgentId(Integer agentId) {
		return QueryChain.of(getMapper())
			.eq(ChatSession::getAgentId, agentId)
			.ne(ChatSession::getStatus, "deleted")
			.orderBy(ChatSession::getIsPinned, false)
			.orderBy(ChatSession::getUpdateTime, false)
			.list();
	}

	/**
	 * 根据会话 ID 查找会话
	 */
	@Override
	public ChatSession findBySessionId(String sessionId) {
		return QueryChain.of(getMapper())
			.eq(ChatSession::getId, sessionId)
			.ne(ChatSession::getStatus, "deleted")
			.one();
	}

	/**
	 * 创建新会话（生成 UUID 作为会话 ID）
	 */
	@Override
	public ChatSession createSession(Integer agentId, String title, String userId) {
		String sessionId = UUID.randomUUID().toString();
		ChatSession session = new ChatSession(sessionId, agentId, title != null ? title : "新会话", "active", userId);
		getMapper().insert(session);
		log.info("Created new chat session: {} for agent: {}", sessionId, agentId);
		return session;
	}

	/**
	 * 清空指定 Agent 的所有会话（软删除）
	 */
	@Override
	public void clearSessionsByAgentId(Integer agentId) {
		LocalDateTime now = LocalDateTime.now();
		int updated = getMapper().softDeleteByAgentId(agentId, now);
		log.info("Cleared {} sessions for agent: {}", updated, agentId);
	}

	/**
	 * 更新会话的最后活动时间
	 */
	@Override
	public void updateSessionTime(String sessionId) {
		LocalDateTime now = LocalDateTime.now();
		getMapper().updateSessionTime(sessionId, now);
	}

	/**
	 * 置顶或取消置顶会话
	 */
	@Override
	public void pinSession(String sessionId, boolean isPinned) {
		LocalDateTime now = LocalDateTime.now();
		getMapper().updatePinStatus(sessionId, isPinned, now);
		log.info("Updated pin status for session: {} to: {}", sessionId, isPinned);
	}

	/**
	 * 重命名会话
	 */
	@Override
	public void renameSession(String sessionId, String newTitle) {
		LocalDateTime now = LocalDateTime.now();
		getMapper().updateTitle(sessionId, newTitle, now);
		log.info("Renamed session: {} to: {}", sessionId, newTitle);
	}

	/**
	 * 删除单个会话（软删除）
	 */
	@Override
	public void deleteSession(String sessionId) {
		LocalDateTime now = LocalDateTime.now();
		getMapper().softDeleteById(sessionId, now);
		log.info("Deleted session: {}", sessionId);
	}

}
