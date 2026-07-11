package com.phoenix.data.util;

import com.phoenix.data.dto.planner.ExecutionStep;
import com.phoenix.data.dto.planner.Plan;
import com.alibaba.cloud.ai.graph.OverAllState;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.phoenix.data.constant.Constant.PLANNER_NODE_OUTPUT;
import static com.phoenix.data.constant.Constant.PLAN_CURRENT_STEP;

/**
 * util class for plan-based execution nodes Provides common functionality for nodes that
 * execute based on predefined plans
 *
 * @author zhangshenghang
 */
public final class PlanProcessUtil {

	private static final BeanOutputConverter<Plan> converter;

	private static final String STEP_PREFIX = "step_";

	static {
		converter = new BeanOutputConverter<>(new ParameterizedTypeReference<>() {
		});
	}

	private PlanProcessUtil() {

	}

	/**
	 * Get the current execution step from the plan
	 * @param state the overall state containing plan information
	 * @return the current execution step
	 * @throws IllegalStateException if plan output is empty, plan parsing fails, or step
	 * index is out of range
	 */
	public static ExecutionStep getCurrentExecutionStep(OverAllState state) {
		Plan plan = getPlan(state);
		int currentStep = getCurrentStepNumber(state);
		return getCurrentExecutionStep(plan, currentStep);
	}

	/**
	 * 获取当前执行步骤的指令
	 * @param state 整体状态
	 * @return 执行指令
	 */
	public static String getCurrentExecutionStepInstruction(OverAllState state) {
		String instruction;
		ExecutionStep.ToolParameters currentStepParams = PlanProcessUtil.getCurrentExecutionStep(state)
			.getToolParameters();
		instruction = currentStepParams != null ? currentStepParams.getInstruction() : "无";
		return instruction;
	}

	/**
	 * Get the current execution step from the plan
	 * @param plan the plan object
	 * @param currentStep current step
	 * @return the current execution step
	 * @throws IllegalStateException if plan output is empty, plan parsing fails, or step
	 * index is out of range
	 */
	public static ExecutionStep getCurrentExecutionStep(Plan plan, Integer currentStep) {
		List<ExecutionStep> executionPlan = plan.getExecutionPlan();
		if (executionPlan == null || executionPlan.isEmpty()) {
			throw new IllegalStateException("执行计划为空");
		}

		int stepIndex = currentStep - 1;
		if (stepIndex < 0 || stepIndex >= executionPlan.size()) {
			throw new IllegalStateException("当前步骤索引超出范围: " + stepIndex);
		}

		return executionPlan.get(stepIndex);
	}

	/**
	 * Get the plan object from state
	 * @param state the overall state containing plan information
	 * @return the parsed plan object
	 * @throws IllegalStateException if plan output is empty or plan parsing fails
	 */
	public static Plan getPlan(OverAllState state) {
		String plannerNodeOutput = (String) state.value(PLANNER_NODE_OUTPUT)
			.orElseThrow(() -> new IllegalStateException("计划节点输出为空"));
		Plan plan = converter.convert(plannerNodeOutput);
		if (plan == null) {
			throw new IllegalStateException("计划解析失败");
		}
		return plan;
	}

	/**
	 * Get the current step number from state
	 * @param state the overall state
	 * @return the current step number (defaults to 1 if not set)
	 */
	public static int getCurrentStepNumber(OverAllState state) {
		return state.value(PLAN_CURRENT_STEP, 1);
	}

	/**
	 * Add step result
	 * @param existingResults existing result collection
	 * @param stepNumber step number
	 * @param result result content
	 * @return updated result collection
	 */
	/**
	 * 添加步骤执行结果到结果集合
	 * @param existingResults 已有结果集合
	 * @param stepNumber 步骤编号
	 * @param result 结果内容
	 * @return 更新后的结果集合
	 */
	public static Map<String, String> addStepResult(Map<String, String> existingResults, Integer stepNumber,
			String result) {
		Map<String, String> updatedResults = new HashMap<>(existingResults);
		updatedResults.put(STEP_PREFIX + stepNumber, result);
		return updatedResults;
	}

}
