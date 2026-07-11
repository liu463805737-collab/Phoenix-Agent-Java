package com.phoenix.data.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent Preset Question Entity Class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("tbl_data_agent_preset_question")
public class AgentPresetQuestion {

	@Id(keyType = KeyType.Auto)
	private Long id;

	private Long agentId;
	private String accountId;

	private String question;
	@Column(ignore = true)
	private String answer;

	private Integer sortOrder;

	private Boolean isActive;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	/**
	 * 构造预设问题对象
	 *
	 * @param agentId 智能体ID
	 * @param question 问题内容
	 * @param sortOrder 排序序号
	 */
	public AgentPresetQuestion(Long agentId, String question, Integer sortOrder) {
		this.agentId = agentId;
		this.question = question;
		this.sortOrder = sortOrder;
		this.isActive = false;
	}

}
