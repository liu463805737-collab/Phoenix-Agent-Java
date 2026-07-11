package com.phoenix.data.dto.schema;

import lombok.Data;

/**
 * 更新逻辑外键的 DTO
 */
@Data
public class UpdateLogicalRelationDTO {

	/**
	 * 主表名（例如 t_order）
	 */
	private String sourceTableName;

	/**
	 * 主表字段名（例如 buyer_uid）
	 */
	private String sourceColumnName;

	/**
	 * 关联表名（例如 t_user）
	 */
	private String targetTableName;

	/**
	 * 关联表字段名（例如 id）
	 */
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
