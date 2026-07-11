package com.phoenix.data.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Semantic Model Configuration Entity Class
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tbl_data_semantic_model")
public class SemanticModel {

	@Id(keyType = KeyType.Auto)
	private Long id;

	/**
	 * 关联的智能体ID
	 */
	private Long agentId;

	/**
	 * 关联的数据源ID
	 */
	private Integer datasourceId;

	/**
	 * 关联的表名
	 */
	private String tableName;

	/**
	 * 数据库中的物理字段名 (例如: csat_score)
	 */
	private String columnName;

	/**
	 * 业务名/别名 (例如: 客户满意度分数)
	 */
	private String businessName;

	/**
	 * 业务名的同义词 (例如: 满意度,客户评分)
	 */
	private String synonyms;

	/**
	 * 业务描述 (用于向LLM解释字段的业务含义)
	 */
	private String businessDescription;

	/**
	 * 数据库中的物理字段的原始注释
	 */
	private String columnComment;

	/**
	 * 物理数据类型 (例如: int, varchar(20))
	 */
	private String dataType;

	/**
	 * 状态: 0 停用 1 启用
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime createdTime;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime updateTime;

	/**
	 * 获取用于提示的信息
	 */
	/**
	 * 获取用于提示信息的格式化字符串
	 *
	 * @return 包含业务名称、表名、字段名等信息的字符串
	 */
	public String getPromptInfo() {
		return String.format("业务名称: %s, 表名: %s, 数据库字段名: %s, 字段同义词: %s, 业务描述: %s, 数据类型: %s", businessName, tableName,
				columnName, synonyms, businessDescription, dataType);
	}

}
