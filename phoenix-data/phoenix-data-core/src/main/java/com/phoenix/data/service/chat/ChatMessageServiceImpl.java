package com.phoenix.data.service.chat;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.data.entity.ChatMessage;
import com.phoenix.data.mapper.ChatMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息服务实现类，提供消息的数据库查询和保存操作。
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
		implements ChatMessageService {

	/**
	 * 根据会话 ID 获取消息列表
	 */
	@Override
	public List<ChatMessage> findBySessionId(String sessionId) {
		return QueryChain.of(getMapper())
			.eq(ChatMessage::getSessionId, sessionId)
			.orderBy(ChatMessage::getCreateTime, true)
			.list();
	}

	/**
	 * 保存消息并返回
	 */
	@Override
	public ChatMessage saveMessage(ChatMessage message) {
		message.setCreateTime(LocalDateTime.now());
		getMapper().insert(message);
		log.info("Saved message: {} for session: {}", message.getId(), message.getSessionId());
		return message;
	}

}
