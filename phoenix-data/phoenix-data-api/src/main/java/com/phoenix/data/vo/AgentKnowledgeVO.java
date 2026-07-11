package com.phoenix.data.vo;

import com.phoenix.data.enums.EmbeddingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent知识视图对象，用于展示知识库中的文档/QA/FAQ条目
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentKnowledgeVO {

	private Integer id;

	private Integer agentId;

	private String title;

	// DOCUMENT, QA, FAQ
	private String type;

	// FAQ QA 问题
	private String question;

	// 当type=QA, FAQ时有内容
	private String content;

	@JsonFormat(shape = JsonFormat.Shape.BOOLEAN)
	private Boolean isRecall;

	// 向量化状态：PENDING待处理，PROCESSING处理中，COMPLETED已完成，FAILED失败
	private EmbeddingStatus embeddingStatus;

	// 操作失败的错误信息
	private String errorMsg;

	// 分块策略类型：token, recursive
	private String splitterType;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime createdTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime updatedTime;

}
