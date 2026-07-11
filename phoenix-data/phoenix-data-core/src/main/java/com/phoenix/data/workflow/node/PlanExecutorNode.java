package com.phoenix.data.workflow.node;

import com.phoenix.data.dto.planner.ExecutionStep;
import com.phoenix.data.dto.planner.Plan;
import com.phoenix.data.util.PlanProcessUtil;
import com.phoenix.data.util.StateUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.phoenix.data.constant.Constant.*;

/**
 * Plan execution and validation node, decides next execution node based on plan, and
 * validates before execution.
 *
 * @author zhangshenghang
 */
@Slf4j
@Component
public class PlanExecutorNode extends AabstractNodeAction {
	@Override
	public String getChName() {
		return "计划生成";
	}

	/**
	 * 支持的节点类型集合
	 */
	private static final Set<String> SUPPORTED_NODES = Set.of(SQL_GENERATE_NODE, PYTHON_GENERATE_NODE,
			REPORT_GENERATOR_NODE);

	/**
	 * 执行计划校验与分发：验证计划结构，根据当前步骤确定下一个执行节点。
	 *
	 * @param state 全局状态
	 * @return 包含下一个节点和校验状态的结果映射
	 */
	@Override
	public Map<String, Object> apply(OverAllState state) throws Exception {
		// TODO 待优化，校验应该在生成计划之后而不是这里，这里导致每次运行一个计划都校验一次
		// 1. Validate the Plan
		Plan plan;
		try {
			plan = PlanProcessUtil.getPlan(state);
		}
		catch (Exception e) {
			log.error("Plan validation failed due to a parsing error.", e);
			return buildValidationResult(state, false,
					"Validation failed: The plan is not a valid JSON structure. Error: " + e.getMessage());
		}

		// Validate execution plan structure
		if (!validateExecutionPlanStructure(plan)) {
			return buildValidationResult(state, false,
					"Validation failed: The generated plan is empty or has no execution steps.");
		}

		// Validate each execution step
		for (ExecutionStep step : plan.getExecutionPlan()) {
			String validationResult = validateExecutionStep(step);
			if (validationResult != null) {
				return buildValidationResult(state, false, validationResult);
			}
		}

		log.info("Plan validation successful.");
		// 2. If开启人工复核，则在执行前暂停，跳转到human_feedback节点
		Boolean humanReviewEnabled = state.value(HUMAN_REVIEW_ENABLED, false);
		if (Boolean.TRUE.equals(humanReviewEnabled)) {
			log.info("Human review enabled: routing to human_feedback node");
			return Map.of(PLAN_VALIDATION_STATUS, true, PLAN_NEXT_NODE, HUMAN_FEEDBACK_NODE);
		}

		int currentStep = PlanProcessUtil.getCurrentStepNumber(state);
		List<ExecutionStep> executionPlan = plan.getExecutionPlan();

		boolean isOnlyNl2Sql = state.value(IS_ONLY_NL2SQL, false);

		// Check if the plan is completed
		if (currentStep > executionPlan.size()) {
			log.info("Plan completed, current step: {}, total steps: {}", currentStep, executionPlan.size());
			return Map.of(PLAN_CURRENT_STEP, 1, PLAN_NEXT_NODE, isOnlyNl2Sql ? StateGraph.END : REPORT_GENERATOR_NODE,
					PLAN_VALIDATION_STATUS, true);
		}

		// Get current step and determine next node
		ExecutionStep executionStep = executionPlan.get(currentStep - 1);
		String toolToUse = executionStep.getToolToUse();

		return determineNextNode(toolToUse);
	}

	/**
	 * 根据工具类型确定下一个执行节点。
	 *
	 * @param toolToUse 工具名称
	 * @return 包含下一个节点和校验状态的结果映射
	 */
	private Map<String, Object> determineNextNode(String toolToUse) {
		if (SUPPORTED_NODES.contains(toolToUse)) {
			log.info("Determined next execution node: {}", toolToUse);
			return Map.of(PLAN_NEXT_NODE, toolToUse, PLAN_VALIDATION_STATUS, true);
		}
		else if (HUMAN_FEEDBACK_NODE.equals(toolToUse)) {
			log.info("Determined next execution node: {}", toolToUse);
			return Map.of(PLAN_NEXT_NODE, toolToUse, PLAN_VALIDATION_STATUS, true);
		}
		else {
			// This case should ideally not be reached if validation is done correctly
			// before.
			return Map.of(PLAN_VALIDATION_STATUS, false, PLAN_VALIDATION_ERROR, "Unsupported node type: " + toolToUse);
		}
	}

	/**
	 * 校验执行计划结构是否有效。
	 *
	 * @param plan 执行计划
	 * @return 结构是否有效
	 */
	private boolean validateExecutionPlanStructure(Plan plan) {
		return plan != null && plan.getExecutionPlan() != null && !plan.getExecutionPlan().isEmpty();
	}

	/**
	 * 校验单个执行步骤的有效性。
	 *
	 * @param step 执行步骤
	 * @return 校验失败时的错误信息，通过则返回 null
	 */
	private String validateExecutionStep(ExecutionStep step) {
		// Validate tool name
		if (step.getToolToUse() == null || !SUPPORTED_NODES.contains(step.getToolToUse())) {
			return "Validation failed: Plan contains an invalid tool name: '" + step.getToolToUse() + "' in step "
					+ step.getStep();
		}

		// Validate tool parameters
		if (step.getToolParameters() == null) {
			return "Validation failed: Tool parameters are missing for step " + step.getStep();
		}

		// Validate specific parameters based on node type
		switch (step.getToolToUse()) {
			case SQL_GENERATE_NODE:
				if (!StringUtils.hasText(step.getToolParameters().getInstruction())) {
					return "Validation failed: SQL generation node is missing description in step " + step.getStep();
				}
				break;

			case PYTHON_GENERATE_NODE:
				if (!StringUtils.hasText(step.getToolParameters().getInstruction())) {
					return "Validation failed: Python generation node is missing instruction in step " + step.getStep();
				}
				break;

			case REPORT_GENERATOR_NODE:
				if (!StringUtils.hasText(step.getToolParameters().getSummaryAndRecommendations())) {
					return "Validation failed: Report generation node is missing summary_and_recommendations in step "
							+ step.getStep();
				}
				break;

			default:
				// This should not happen due to the earlier validation
				break;
		}

		return null; // Validation passed
	}

	/**
	 * 构建校验结果映射，失败时增加修复计数。
	 *
	 * @param state 全局状态
	 * @param isValid 是否通过校验
	 * @param errorMessage 错误信息
	 * @return 校验结果映射
	 */
	private Map<String, Object> buildValidationResult(OverAllState state, boolean isValid, String errorMessage) {
		if (isValid) {
			return Map.of(PLAN_VALIDATION_STATUS, true);
		}
		else {
			// When validation fails, increment the repair count here.
			int repairCount = StateUtil.getObjectValue(state, PLAN_REPAIR_COUNT, Integer.class, 0);
			return Map.of(PLAN_VALIDATION_STATUS, false, PLAN_VALIDATION_ERROR, errorMessage, PLAN_REPAIR_COUNT,
					repairCount + 1);
		}
	}

}
