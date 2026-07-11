package com.phoenix.data.dto.schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建逻辑外键的 DTO
 */
@Data
public class CreateLogicalRelationDTO {

	/**
	 * 主表名（例如 t_order）
	 */
	@NotBlank(message = "主表名不能为空")
	private String sourceTableName;

	/**
	 * 主表字段名（例如 buyer_uid）
	 */
	@NotBlank(message = "主表字段名不能为空")
	private String sourceColumnName;

	/**
	 * 关联表名（例如 t_user）
	 */
	@NotBlank(message = "关联表名不能为空")
	private String targetTableName;

	/**
	 * 关联表字段名（例如 id）
	 */
	@NotBlank(message = "关联表字段名不能为空")
	private String targetColumnName;

	/**
	 * 关系类型（可选） 1:1, 1:N, N:1 - 辅助LLM理解数据基数
	 */
	private String relationType;

	/**
	 * 业务描述（可选）
	 */
	private String description;

}
