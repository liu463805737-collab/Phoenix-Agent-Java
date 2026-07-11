package com.phoenix.data.dto.knowledge.agentknowledge;

import lombok.Data;

/**
 * 更新知识 DTO
 */
@Data
public class UpdateKnowledgeDTO {

	/**
	 * 知识标题
	 */
	private String title;

	// 不更新question，只更新title和content。question要更新直接创建新的知识
	/**
	 * 内容（当type=QA, FAQ时必填）
	 */
	private String content;

}
