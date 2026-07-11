package com.phoenix.data.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Chat Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

	private String sessionId;

	private String message;

	private String messageType; // text, sql, result, error

	private String sql; // Generated SQL statement

	private Object result; // Query result

	private String error; // Error message

	/**
	 * 构造方法
	 * @param sessionId 会话ID
	 * @param message 消息内容
	 * @param messageType 消息类型
	 */
	public ChatResponse(String sessionId, String message, String messageType) {
		this.sessionId = sessionId;
		this.message = message;
		this.messageType = messageType;
	}

}
