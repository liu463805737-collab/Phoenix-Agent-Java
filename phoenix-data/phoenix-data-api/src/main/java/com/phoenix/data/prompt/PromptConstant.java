package com.phoenix.data.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;

/**
 * Prompt constant class, dynamically loads prompt files
 *
 * @author zhangshenghang
 */
public class PromptConstant {

	/**
	 * 获取意图识别提示词模板
	 */
	public static PromptTemplate getIntentRecognitionPromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("intent-recognition"));
	}

	/**
	 * 获取证据查询重写提示词模板
	 */
	// evidence-query-rewrite
	public static PromptTemplate getEvidenceQueryRewritePromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("evidence-query-rewrite"));
	}

	/**
	 * 获取智能体知识提示词模板
	 */
	// agent-knowledge.txt
	public static PromptTemplate getAgentKnowledgePromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("agent-knowledge"));
	}

	/**
	 * 获取查询增强提示词模板
	 */
	public static PromptTemplate getQueryEnhancementPromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("query-enhancement"));
	}

	/**
	 * 获取可行性评估提示词模板
	 */
	// feasibility-assessment
	public static PromptTemplate getFeasibilityAssessmentPromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("feasibility-assessment"));
	}

	/**
	 * 获取混合选择器提示词模板
	 */
	public static PromptTemplate getMixSelectorPromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("mix-selector"));
	}

	/**
	 * 获取语义一致性检查提示词模板
	 */
	public static PromptTemplate getSemanticConsistencyPromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("semantic-consistency"));
	}

	/**
	 * 获取SQL生成提示词模板
	 */
	public static PromptTemplate getNewSqlGeneratorPromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("new-sql-generate"));
	}

	/**
	 * 获取规划器提示词模板
	 */
	public static PromptTemplate getPlannerPromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("planner"));
	}

	/**
	 * 获取纯文本报告生成提示词模板
	 */
	public static PromptTemplate getReportGeneratorPlainPromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("report-generator-plain"));
	}

	/**
	 * 获取SQL错误修复提示词模板
	 */
	public static PromptTemplate getSqlErrorFixerPromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("sql-error-fixer"));
	}

	/**
	 * 获取Python代码生成提示词模板
	 */
	public static PromptTemplate getPythonGeneratorPromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("python-generator"));
	}

	/**
	 * 获取Python分析结果提示词模板
	 */
	public static PromptTemplate getPythonAnalyzePromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("python-analyze"));
	}

	/**
	 * 获取业务知识提示词模板
	 */
	public static PromptTemplate getBusinessKnowledgePromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("business-knowledge"));
	}

	/**
	 * 获取语义模型提示词模板
	 */
	public static PromptTemplate getSemanticModelPromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("semantic-model"));
	}

	/**
	 * 获取JSON修复提示词模板
	 */
	public static PromptTemplate getJsonFixPromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("json-fix"));
	}

	/**
	 * 获取数据视图分析提示词模板
	 */
	public static PromptTemplate getDataViewAnalyzePromptTemplate() {
		return new PromptTemplate(PromptLoader.loadPrompt("data-view-analyze"));
	}

}
