package com.phoenix.data.service.chat;

import com.mybatisflex.core.service.IService;
import com.phoenix.data.entity.ChatMessage;

import java.util.List;

/**
 * 聊天消息服务接口，提供消息的查询和保存功能。
 */
public interface ChatMessageService extends IService<ChatMessage> {

	/**
	 * 根据会话 ID 获取消息列表
	 */
	List<ChatMessage> findBySessionId(String sessionId);

	/**
	 * 保存消息
	 */
	ChatMessage saveMessage(ChatMessage message);

}
