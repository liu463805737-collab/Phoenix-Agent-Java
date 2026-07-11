package com.phoenix.data.service.chat;

import com.mybatisflex.core.service.IService;
import com.phoenix.data.entity.ChatSession;

import java.util.List;

/**
 * 聊天会话服务接口，提供会话的 CRUD、置顶、重命名及软删除功能。
 */
public interface ChatSessionService extends IService<ChatSession> {

	/**
	 * 根据Agent ID 和用户id 获取会话列表
	 * @return
	 */
	List<ChatSession> findByAgentIdAndUserId(Integer agentId, String userId);

	/**
	 * 根据 Agent ID 获取会话列表
	 */
	List<ChatSession> findByAgentId(Integer agentId);

	/**
	 * 创建新会话
	 */
	ChatSession createSession(Integer agentId, String title, String userId);

	/**
	 * 根据会话 ID 查找会话
	 */
	ChatSession findBySessionId(String sessionId);

	/**
	 * 清空指定 Agent 的所有会话（软删除）
	 */
	void clearSessionsByAgentId(Integer agentId);

	/**
	 * 更新会话的最后活动时间
	 */
	void updateSessionTime(String sessionId);

	/**
	 * 置顶或取消置顶会话
	 */
	void pinSession(String sessionId, boolean isPinned);

	/**
	 * 重命名会话
	 */
	void renameSession(String sessionId, String newTitle);

	/**
	 * 删除单个会话（软删除）
	 */
	void deleteSession(String sessionId);

}
