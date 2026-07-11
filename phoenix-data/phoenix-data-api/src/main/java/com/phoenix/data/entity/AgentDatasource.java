package com.phoenix.data.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能体数据源关联实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("tbl_data_agent_datasource")
public class AgentDatasource {

	@Id(keyType = KeyType.Auto)
	private Integer id;

	private Long agentId;

	private Integer datasourceId;

	private Integer isActive;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateTime;

	@Column(ignore = true)
	private Datasource datasource;

	@Column(ignore = true)
	private List<String> selectTables;

	/**
	 * 构造智能体数据源关联对象
	 *
	 * @param agentId 智能体ID
	 * @param datasourceId 数据源ID
	 */
	public AgentDatasource(Long agentId, Integer datasourceId) {
		this.agentId = agentId;
		this.datasourceId = datasourceId;
		this.isActive = 1;
	}

}
