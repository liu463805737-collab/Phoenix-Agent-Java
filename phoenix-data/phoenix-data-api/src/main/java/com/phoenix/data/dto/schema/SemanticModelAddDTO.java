package com.phoenix.data.dto.schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 添加语义模型的DTO类 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemanticModelAddDTO {

	/** 关联的智能体ID */
	@NotNull(message = "智能体ID不能为空")
	private Long agentId;

	/** 关联的表名 */
	@NotBlank(message = "表名不能为空")
	private String tableName;

	/** 数据库中的物理字段名 (例如: csat_score) */
	@NotBlank(message = "数据库字段名不能为空")
	private String columnName;

	/** 业务名/别名 (例如: 客户满意度分数) */
	@NotBlank(message = "业务名称不能为空")
	private String businessName;

	/** 业务名的同义词 (例如: 满意度,客户评分) */
	private String synonyms;

	/** 业务描述 (用于向LLM解释字段的业务含义) */
	private String businessDescription;

	/** 数据库中的物理字段的原始注释 */
	private String columnComment;

	/** 物理数据类型 (例如: int, varchar(20)) */
	@NotBlank(message = "数据类型不能为空")
	private String dataType;

}
