package com.phoenix.data.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 逻辑外键配置实体类 用于定义数据源中表之间的逻辑外键关系，帮助 LLM 理解数据关联
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("tbl_data_logical_relation")
public class LogicalRelation {

	@Id(keyType = KeyType.Auto)
	private Integer id;

	/**
	 * 关联的数据源ID
	 */
	private Integer datasourceId;

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
	 * 业务描述（可选） 存入Prompt中帮助LLM理解 例如："订单表通过buyer_uid关联用户表id"
	 */
	private String description;

	/**
	 * 逻辑删除标志 0-未删除, 1-已删除
	 */
	private Integer isDeleted;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdTime;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedTime;

}
