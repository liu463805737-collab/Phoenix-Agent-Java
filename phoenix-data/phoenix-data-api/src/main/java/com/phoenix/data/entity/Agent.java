package com.phoenix.data.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.phoenix.data.enums.AgentStatusEnm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent Entity Class
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table("tbl_data_agent")
public class Agent {
	@Id(keyType = KeyType.Auto)
	private Long id;
	/**
	 * @see com.phoenix.common.enm.AgentTypeEnm 智能体类型
	 */
	private String type = "sql";
	private String categoryId;
	private String sn;
	private String name; // Agent name

	private String description; // Agent description

	private String avatar; // Avatar URL

	/**
	 * @see AgentStatusEnm
 	 */
	private String status; // Status: draft-pending publication, published-published,
							// offline-offline

	@JsonIgnore
	private String apiKey; // API Key for external access, format sk-xxx

	@Builder.Default
	private Integer apiKeyEnabled = 0; // 0/1 toggle for API access

	private String prompt; // Custom Prompt configuration

	private String category; // Category

	private Long adminId; // Admin ID

	private String tags; // Tags, comma separated
	private Integer orderNum;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime updateTime;

}
