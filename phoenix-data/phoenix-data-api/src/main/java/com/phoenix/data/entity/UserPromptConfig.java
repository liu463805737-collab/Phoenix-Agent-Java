package com.phoenix.data.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户提示词配置实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("tbl_data_user_prompt_config")
public class UserPromptConfig {

	@Id
	private String id;

	/**
	 * Configuration name
	 */
	private String name;

	/**
	 * Prompt type (e.g., report-generator, planner, etc.)
	 */
	private String promptType;

	/**
	 * Associated agent ID, null means global configuration
	 */
	private Long agentId;

	/**
	 * User-defined system prompt content
	 */
	private String systemPrompt;

	/**
	 * Whether to enable this configuration
	 */
	@Builder.Default
	private Boolean enabled = true;

	/**
	 * Configuration description
	 */
	private String description;

	/**
	 * Configuration priority (higher number = higher priority)
	 */
	@Builder.Default
	private Integer priority = 0;

	/**
	 * Configuration order for display
	 */
	@Builder.Default
	private Integer displayOrder = 0;

	/**
	 * Creation time
	 */
	private LocalDateTime createTime;

	/**
	 * Update time
	 */
	private LocalDateTime updateTime;

	/**
	 * Creator
	 */
	private String creator;

	/**
	 * 获取优化提示词内容
	 *
	 * @return 系统提示词内容
	 */
	public String getOptimizationPrompt() {
		return this.systemPrompt;
	}

	/**
	 * 设置优化提示词内容
	 *
	 * @param optimizationPrompt 优化提示词
	 */
	public void setOptimizationPrompt(String optimizationPrompt) {
		this.systemPrompt = optimizationPrompt;
	}

}
