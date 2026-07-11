package com.phoenix.data.workflow.node;

import cn.hutool.core.date.DateUtil;
import com.phoenix.data.dto.planner.ExecutionStep;
import com.phoenix.data.dto.planner.Plan;
import com.phoenix.data.entity.UserPromptConfig;
import com.phoenix.data.enums.TextType;
import com.phoenix.data.prompt.PromptHelper;
import com.phoenix.data.service.llm.LlmService;
import com.phoenix.data.service.prompt.UserPromptService;
import com.phoenix.data.util.ChatResponseUtil;
import com.phoenix.data.utils.FluxUtil;
import com.phoenix.data.util.StateUtil;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.phoenix.data.constant.Constant.*;

/**
 * Report generation node that creates comprehensive analysis reports based on execution
 * results.
 *
 * This node is responsible for: - Generating detailed analysis reports from SQL execution
 * results - Summarizing data insights and findings - Providing comprehensive answers to
 * user queries - Creating structured final output for users
 *
 * @author zhangshenghang
 */
@Slf4j
@Component
public class ReportGeneratorNode extends AabstractNodeAction {

	private final LlmService llmService;

	private final BeanOutputConverter<Plan> converter;

	private final UserPromptService promptConfigService;

	@Override
	public String getChName() {
		return "生成报表";
	}

	/**
	 * 构造报告生成节点。
	 *
	 * @param llmService LLM 服务
	 * @param promptConfigService 提示词配置服务
	 */
	public ReportGeneratorNode(LlmService llmService, UserPromptService promptConfigService) {
		this.llmService = llmService;
		this.converter = new BeanOutputConverter<>(new ParameterizedTypeReference<>() {
		});
		this.promptConfigService = promptConfigService;
	}

	/**
	 * 执行报告生成：根据执行结果和计划生成综合分析报告。
	 *
	 * @param state 全局状态
	 * @return 包含报告内容的结果映射
	 */
	@Override
	public Map<String, Object> apply(OverAllState state) throws Exception {

		// Get necessary input parameters
		String plannerNodeOutput = StateUtil.getStringValue(state, PLANNER_NODE_OUTPUT);
		String userInput = StateUtil.getCanonicalQuery(state);
		Integer currentStep = StateUtil.getObjectValue(state, PLAN_CURRENT_STEP, Integer.class, 1);
		@SuppressWarnings("unchecked")
		HashMap<String, String> executionResults = StateUtil.getObjectValue(state, SQL_EXECUTE_NODE_OUTPUT,
				HashMap.class, new HashMap<>());

		// Parse plan and get current step
		Plan plan = converter.convert(plannerNodeOutput);
		ExecutionStep executionStep = getCurrentExecutionStep(plan, currentStep);
		String summaryAndRecommendations = executionStep.getToolParameters().getSummaryAndRecommendations();

		// Get agent id from state
		String agentIdStr = StateUtil.getStringValue(state, AGENT_ID);
		Long agentId = null;
		try {
			if (agentIdStr != null) {
				agentId = Long.parseLong(agentIdStr);
			}
		}
		catch (NumberFormatException ignore) {
			// ignore parse error, treat as global config
		}

		// Generate report streaming flux
		Flux<ChatResponse> reportGenerationFlux = generateReport(userInput, plan, executionResults,
				summaryAndRecommendations, agentId);

		TextType reportTextType = TextType.MARK_DOWN;

		// Use utility class to create streaming generator with content collection
		Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGeneratorWithMessages(this.getChName(), this.getClass(),
				state, "开始生成报告...", "报告生成完成！", reportContent -> {
					log.info("Generated report content: {}", reportContent);
					Map<String, Object> result = new HashMap<>();
					result.put(RESULT, reportContent);
					result.put(SQL_EXECUTE_NODE_OUTPUT, null);
					result.put(PLAN_CURRENT_STEP, null);
					result.put(PLANNER_NODE_OUTPUT, null);
					return result;
				},
				Flux.concat(Flux.just(ChatResponseUtil.createPureResponse(reportTextType.getStartSign())),
						reportGenerationFlux,
						Flux.just(ChatResponseUtil.createPureResponse(reportTextType.getEndSign()))));

		return Map.of(RESULT, generator);
	}

	/**
	 * 从计划中获取当前执行步骤。
	 *
	 * @param plan 执行计划
	 * @param currentStep 当前步骤编号
	 * @return 当前执行步骤
	 */
	private ExecutionStep getCurrentExecutionStep(Plan plan, Integer currentStep) {
		List<ExecutionStep> executionPlan = plan.getExecutionPlan();
		if (executionPlan == null || executionPlan.isEmpty()) {
			throw new IllegalStateException("Execution plan is empty");
		}

		int stepIndex = currentStep - 1;
		if (stepIndex < 0 || stepIndex >= executionPlan.size()) {
			throw new IllegalStateException("Current step index out of range: " + stepIndex);
		}

		return executionPlan.get(stepIndex);
	}

	/**
	 * 生成分析报告：结合用户需求、执行计划和数据结果。
	 *
	 * @param userInput 用户输入
	 * @param plan 执行计划
	 * @param executionResults 执行结果
	 * @param summaryAndRecommendations 总结和建议
	 * @param agentId 智能体 ID
	 * @return LLM 响应流
	 */
	private Flux<ChatResponse> generateReport(String userInput, Plan plan, HashMap<String, String> executionResults,
			String summaryAndRecommendations, Long agentId) {
		// Build user requirements and plan description
		String userRequirementsAndPlan = buildUserRequirementsAndPlan(userInput, plan);

		// Build analysis steps and data results description
		String analysisStepsAndData = buildAnalysisStepsAndData(plan, executionResults);

		// Get optimization configs if available (优先按智能体加载)
		List<UserPromptConfig> optimizationConfigs = promptConfigService.getOptimizationConfigs("report-generator",
				agentId);
		//当前时间
		String currDate = DateUtil.formatDate(new Date());

		String reportPrompt = PromptHelper.buildReportGeneratorPromptWithOptimization(userRequirementsAndPlan,
				analysisStepsAndData, summaryAndRecommendations, optimizationConfigs, currDate);
		log.debug("Report Node Prompt: \n {} \n", reportPrompt);
		return llmService.callUser(reportPrompt);
	}

	/**
	 * 构建用户需求和计划描述文本。
	 *
	 * @param userInput 用户输入
	 * @param plan 执行计划
	 * @return 格式化后的需求和计划描述
	 */
	private String buildUserRequirementsAndPlan(String userInput, Plan plan) {
		StringBuilder sb = new StringBuilder();
		sb.append("## 用户原始需求\n");
		sb.append(userInput).append("\n\n");

		sb.append("## 执行计划概述\n");
		sb.append("**思考过程**: ").append(plan.getThoughtProcess()).append("\n\n");

		sb.append("## 详细执行步骤\n");
		List<ExecutionStep> executionPlan = plan.getExecutionPlan();
		for (int i = 0; i < executionPlan.size(); i++) {
			ExecutionStep step = executionPlan.get(i);
			sb.append("### 步骤 ").append(i + 1).append(": 步骤编号 ").append(step.getStep()).append("\n");
			sb.append("**工具**: ").append(step.getToolToUse()).append("\n");
			if (step.getToolParameters() != null) {
				sb.append("**参数描述**: ").append(step.getToolParameters().getInstruction()).append("\n");
			}
			sb.append("\n");
		}

		return sb.toString();
	}

	/**
	 * 构建分析步骤和数据结果描述文本。
	 *
	 * @param plan 执行计划
	 * @param executionResults 执行结果
	 * @return 格式化后的分析步骤和数据描述
	 */
	private String buildAnalysisStepsAndData(Plan plan, HashMap<String, String> executionResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("## 数据执行结果\n");

		if (executionResults.isEmpty()) {
			sb.append("暂无执行结果数据\n");
		}
		else {
			List<ExecutionStep> executionPlan = plan.getExecutionPlan();
			for (int i = 0; i < executionPlan.size(); i++) {
				ExecutionStep step = executionPlan.get(i);
				String stepId = String.valueOf(i + 1);
				String stepKey = "step_" + stepId;
				String stepResult = executionResults.get(stepKey);
				String analysisResult = executionResults.get(stepKey + "_analysis");

				if ((stepResult == null || stepResult.trim().isEmpty())
						&& (analysisResult == null || analysisResult.trim().isEmpty())) {
					continue;
				}

				sb.append("### ").append(stepKey).append("\n");
				sb.append("**步骤编号**: ").append(step.getStep()).append("\n");
				sb.append("**使用工具**: ").append(step.getToolToUse()).append("\n");
				if (step.getToolParameters() != null) {
					sb.append("**参数描述**: ").append(step.getToolParameters().getInstruction()).append("\n");
					if (step.getToolParameters().getSqlQuery() != null) {
						sb.append("**执行SQL**: \n```sql\n")
							.append(step.getToolParameters().getSqlQuery())
							.append("\n```\n");
					}
				}

				if (stepResult != null && !stepResult.trim().isEmpty()) {
					sb.append("**执行结果**: \n```json\n").append(stepResult).append("\n```\n\n");
				}
				if (analysisResult != null && !analysisResult.trim().isEmpty()) {
					sb.append("**Python 分析结果**: ").append(analysisResult).append("\n\n");
				}
			}
		}

		return sb.toString();
	}

}
