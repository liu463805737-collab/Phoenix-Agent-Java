package com.phoenix.data.dto.knowledge.agentknowledge;

import com.phoenix.common.annotation.InEnum;
import com.phoenix.data.enums.KnowledgeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 创建知识DTO
 */
@Data
public class CreateKnowledgeDTO {

	/**
	 * 智能体ID
	 */
	@NotNull(message = "智能体ID不能为空")
	private Integer agentId;

	/**
	 * 知识标题
	 */
	@NotBlank(message = "知识标题不能为空")
	private String title;

	/**
	 * 知识类型：DOCUMENT, QA, FAQ
	 */
	@NotBlank(message = "知识类型不能为空")
	@InEnum(value = KnowledgeType.class, message = "type只能是DOCUMENT/QA/FAQ 之一")
	private String type;

	/**
	 * 问题（FAQ和QA类型时必填）
	 */
	private String question;

	/**
	 * 内容（当type=QA, FAQ时必填）
	 */
	private String content;

	/**
	 * 上传的文件（当type=DOCUMENT时必填）
	 */
	private MultipartFile file;

	/**
	 * 分块策略类型：token, recursive 默认值是 token
	 */
	private String splitterType;

}
