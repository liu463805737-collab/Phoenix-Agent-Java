package com.phoenix.data.entity;

import com.phoenix.data.enums.EmbeddingStatus;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Business Knowledge Management Entity Class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("tbl_data_business_knowledge")
public class BusinessKnowledge {

	@Id(keyType = KeyType.Auto)
	private Long id;

	private String businessTerm; // Business term

	private String description; // Description

	private String synonyms; // Synonyms, comma-separated

	@Builder.Default
	private Integer isRecall = 1; // Whether to recall (0: not recall, 1: recall)

	private Long agentId; // Associated agent ID

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime createdTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime updatedTime;

	// 向量化状态：PENDING待处理，PROCESSING处理中，COMPLETED已完成，FAILED失败
	private EmbeddingStatus embeddingStatus;

	// 操作失败的错误信息
	private String errorMsg;

	// 0=未删除, 1=已删除
	@Builder.Default
	private Integer isDeleted = 0;

}
