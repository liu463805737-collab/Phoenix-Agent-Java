package com.phoenix.data.entity;

import com.phoenix.data.enums.EmbeddingStatus;
import com.phoenix.data.enums.KnowledgeType;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent Knowledge Entity Class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("tbl_data_agent_knowledge")
public class AgentKnowledge {

	@Id(keyType = KeyType.Auto)
	private Integer id;

	private Integer agentId;

	private String title;

	// DOCUMENT, QA, FAQ
	private KnowledgeType type;

	// FAQ QA 问题
	private String question;

	// 当type=QA, FAQ时有内容
	private String content;

	// 业务状态: 1=召回, 0=非召回
	private Integer isRecall;

	// 向量化状态：PENDING待处理，PROCESSING处理中，COMPLETED已完成，FAILED失败
	private EmbeddingStatus embeddingStatus;

	// 操作失败的错误信息
	private String errorMsg;

	private String sourceFilename;

	// 文件路径
	private String filePath;

	// 文件大小（字节）
	private Long fileSize;

	// 文件类型
	private String fileType;

	// 分块策略类型：token, recursive
	// 默认值是 token
	private String splitterType;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime createdTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime updatedTime;

	// 0=未删除, 1=已删除
	private Integer isDeleted;

	// 0=物理资源（文件和向量）未清理, 1=物理资源已清理
	// 默认值是 0
	private Integer isResourceCleaned;

}
