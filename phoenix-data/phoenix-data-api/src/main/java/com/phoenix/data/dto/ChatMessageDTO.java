package com.phoenix.data.dto;

import lombok.Data;

/**
 * Request payload for saving chat messages.
 */
@Data
public class ChatMessageDTO {

	private String role;

	private String content;

	private String messageType;

	private String metadata;

	/**
	 * Flag from frontend to trigger async title generation for newly created sessions.
	 */
	private boolean titleNeeded;

}
