package com.phoenix.data.dto.planner;

import com.phoenix.data.constant.Constant;
import com.phoenix.data.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 执行计划 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

	@JsonProperty("thought_process")
	@JsonPropertyDescription("简要描述你的分析思路。必须明确提到你检查了哪些表和字段")
	private String thoughtProcess;

	@JsonProperty("execution_plan")
	@JsonPropertyDescription("执行计划的步骤列表")
	private List<ExecutionStep> executionPlan;

	/**
	 * 返回计划的字符串表示
	 */
	@Override
	public String toString() {
		return "Plan{" + "thoughtProcess='" + thoughtProcess + '\'' + ", executionPlan=" + executionPlan + '}';
	}

	// 为NL2SQL模式准备的Plan，只走SQL生成
	private static final String NL2SQL_PLAN_JSON;

	static {
		ExecutionStep step = new ExecutionStep();
		ExecutionStep.ToolParameters parameters = new ExecutionStep.ToolParameters();
		parameters.setInstruction("SQL生成");
		step.setStep(1);
		step.setToolToUse(Constant.SQL_GENERATE_NODE);
		step.setToolParameters(parameters);
		Plan plan = new Plan();
		plan.setThoughtProcess("根据问题生成SQL");
		plan.setExecutionPlan(List.of(step));
		try {
			NL2SQL_PLAN_JSON = JsonUtil.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(plan);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取 NL2SQL 模式的默认执行计划 JSON
	 */
	public static String nl2SqlPlan() {
		return NL2SQL_PLAN_JSON;
	}

}
