package com.phoenix.data.dto.planner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 执行步骤 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionStep {

	@JsonProperty("step")
	@JsonPropertyDescription("步骤顺序号")
	private int step;

	@JsonProperty("tool_to_use")
	@JsonPropertyDescription("工具名称")
	private String toolToUse;

	@JsonProperty("tool_parameters")
	@JsonPropertyDescription("工具参数")
	private ToolParameters toolParameters;

	/**
	 * 工具参数 DTO
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	// 序列化时忽略 null 值，让生成的 JSON 更干净
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ToolParameters {

		// 统一指令字段：
		// 1. SQL_GENERATE_NODE -> 存当前步骤要做的详细的 SQL 需求
		// 2. PYTHON_GENERATE_NODE -> 存当前步骤要做的详细的编程需求
		@JsonProperty("instruction")
		@JsonPropertyDescription("当工具名称tool_to_use是SQL_GENERATE_NODE时这里的值为 当前步骤要做的详细的 SQL 需求，是PYTHON_GENERATE_NODE时填当前步骤要做的详细的编程需求")
		private String instruction;

		// REPORT_GENERATOR_NODE专用。报告的大纲、需要回答的关键问题和建议方向。
		@JsonProperty("summary_and_recommendations")
		@JsonPropertyDescription("REPORT_GENERATOR_NODE节点专用,仅 REPORT 节点需要此字段，报告的大纲")
		private String summaryAndRecommendations;

		// --- 运行态字段 ---
		// Planner 永远不填这个字段（Prompt里根本不提它）
		// 但是 SQL_GENERATE_NODE 运行完后，会把生成的 SQL 塞进来
		@JsonProperty("sql_query")
		@JsonPropertyDescription("SQL_GENERATE_NODE 运行完后，会把生成的 SQL 塞进来")
		private String sqlQuery;

	}

	/**
	 * 返回执行步骤的字符串表示
	 */
	@Override
	public String toString() {
		return "ExecutionStep{" + "step=" + step + ", toolToUse='" + toolToUse + '\'' + ", toolParameters="
				+ toolParameters + '}';
	}

}
