package com.phoenix.data.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 智能体数据源表选择实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("tbl_data_agent_datasource_tables")
public class AgentDatasourceTables {

	@Id(keyType = KeyType.Auto)
	private Integer id;

	private Integer agentDatasourceId;

	private String tableName;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

}
