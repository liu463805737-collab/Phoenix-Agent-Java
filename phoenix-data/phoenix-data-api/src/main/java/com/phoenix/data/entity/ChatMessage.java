package com.phoenix.data.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Chat Message Entity Class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("tbl_data_chat_message")
public class ChatMessage {

	@Id(keyType = KeyType.Auto)
	private Long id;

	private String sessionId;

	private String role; // user, assistant, system

	private String content;

	private String messageType; // text, sql, result, error

	private String metadata; // JSON格式的元数据

	private LocalDateTime createTime;

}
