package com.phoenix.data.prompt;

import cn.hutool.core.collection.CollUtil;
import com.phoenix.data.bo.schema.DisplayStyleBO;
import com.phoenix.data.dto.prompt.*;
import com.phoenix.data.dto.schema.ColumnDTO;
import com.phoenix.data.dto.schema.SchemaDTO;
import com.phoenix.data.dto.schema.TableDTO;
import com.phoenix.data.entity.SemanticModel;
import com.phoenix.data.entity.UserPromptConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.phoenix.data.util.ReportTemplateUtil.cleanJsonExample;

/**
 * 提示词构建工具类，提供各类提示词的装配方法
 */
public class PromptHelper {

	/**
	 * 构建混合选择器提示词
	 * @param evidence 参考信息
	 * @param question 用户问题
	 * @param schemaDTO 数据库Schema
	 * @return 渲染后的提示词
	 */
	public static String buildMixSelectorPrompt(String evidence, String question, SchemaDTO schemaDTO) {
		String schemaInfo = buildMixMacSqlDbPrompt(schemaDTO, true);
		Map<String, Object> params = new HashMap<>();
		params.put("schema_info", schemaInfo);
		params.put("question", question);
		if (StringUtils.isBlank(evidence))
			params.put("evidence", "无");
		else
			params.put("evidence", evidence);
		return PromptConstant.getMixSelectorPromptTemplate().render(params);
	}

	/**
	 * 构建数据库Schema提示词文本
	 * @param schemaDTO 数据库Schema
	 * @param withColumnType 是否包含列类型信息
	 * @return Schema文本
	 */
	public static String buildMixMacSqlDbPrompt(SchemaDTO schemaDTO, Boolean withColumnType) {
		StringBuilder sb = new StringBuilder();
		sb.append("【DB_ID】 ").append(schemaDTO.getName() == null ? "" : schemaDTO.getName()).append("\n");
		for (TableDTO tableDTO : schemaDTO.getTable()) {
			sb.append(buildMixMacSqlTablePrompt(tableDTO, withColumnType)).append("\n");
		}
		if (CollUtil.isNotEmpty(schemaDTO.getForeignKeys())) {
			sb.append("【Foreign keys】\n").append(StringUtils.join(schemaDTO.getForeignKeys(), "\n"));
		}
		return sb.toString();
	}

	/**
	 * 构建单表的Schema提示词文本
	 * @param tableDTO 表信息
	 * @param withColumnType 是否包含列类型
	 * @return 表描述文本
	 */
	public static String buildMixMacSqlTablePrompt(TableDTO tableDTO, Boolean withColumnType) {
		StringBuilder sb = new StringBuilder();
		// sb.append("# Table:
		// ").append(tableDTO.getName()).append(StringUtils.isBlank(tableDTO.getDescription())
		// ? "" : ", " + tableDTO.getDescription()).append("\n");
		sb.append("# Table: ").append(tableDTO.getName());
		if (!StringUtils.equals(tableDTO.getName(), tableDTO.getDescription())) {
			sb.append(StringUtils.isBlank(tableDTO.getDescription()) ? "" : ", " + tableDTO.getDescription())
				.append("\n");
		}
		else {
			sb.append("\n");
		}
		sb.append("[\n");
		List<String> columnLines = new ArrayList<>();
		for (ColumnDTO columnDTO : tableDTO.getColumn()) {
			StringBuilder line = new StringBuilder();
			line.append("(")
				.append(columnDTO.getName())
				.append(BooleanUtils.isTrue(withColumnType)
						? ":" + StringUtils.defaultString(columnDTO.getType(), "").toUpperCase(Locale.ROOT) : "");
			if (!StringUtils.equals(columnDTO.getDescription(), columnDTO.getName())) {
				line.append(", ").append(StringUtils.defaultString(columnDTO.getDescription(), ""));
			}
			if (CollUtil.isNotEmpty(tableDTO.getPrimaryKeys())
					&& tableDTO.getPrimaryKeys().contains(columnDTO.getName())) {
				line.append(", Primary Key");
			}
			List<String> enumData = Optional.ofNullable(columnDTO.getData())
				.orElse(new ArrayList<>())
				.stream()
				.filter(d -> !StringUtils.isEmpty(d))
				.collect(Collectors.toList());
			if (CollUtil.isNotEmpty(enumData) && !"id".equals(columnDTO.getName())) {
				line.append(", Examples: [");
				List<String> data = new ArrayList<>(enumData.subList(0, Math.min(3, enumData.size())));
				line.append(StringUtils.join(data, ",")).append("]");
			}

			line.append(")");
			columnLines.add(line.toString());
		}
		sb.append(StringUtils.join(columnLines, ",\n"));
		sb.append("\n]");
		return sb.toString();
	}

	/**
	 * 构建SQL生成提示词
	 * @param sqlGenerationDTO SQL生成参数
	 * @return 渲染后的提示词
	 */
	public static String buildNewSqlGeneratorPrompt(SqlGenerationDTO sqlGenerationDTO) {
		String schemaInfo = buildMixMacSqlDbPrompt(sqlGenerationDTO.getSchemaDTO(), true);
		Map<String, Object> params = new HashMap<>();
		params.put("dialect", sqlGenerationDTO.getDialect());
		params.put("question", sqlGenerationDTO.getQuery());
		params.put("schema_info", schemaInfo);
		params.put("evidence", sqlGenerationDTO.getEvidence());
		params.put("execution_description", sqlGenerationDTO.getExecutionDescription());
		return PromptConstant.getNewSqlGeneratorPromptTemplate().render(params);
	}

	/**
	 * 构建语义一致性检查提示词
	 * @param semanticConsistencyDTO 语义一致性检查参数
	 * @return 渲染后的提示词
	 */
	public static String buildSemanticConsistenPrompt(SemanticConsistencyDTO semanticConsistencyDTO) {
		Map<String, Object> params = new HashMap<>();
		params.put("dialect", semanticConsistencyDTO.getDialect());
		params.put("execution_description", semanticConsistencyDTO.getExecutionDescription());
		params.put("user_query", semanticConsistencyDTO.getUserQuery());
		params.put("evidence", semanticConsistencyDTO.getEvidence());
		params.put("schema_info", semanticConsistencyDTO.getSchemaInfo());
		params.put("sql", semanticConsistencyDTO.getSql());
		return PromptConstant.getSemanticConsistencyPromptTemplate().render(params);
	}

	/**
	 * Build report generation prompt with custom prompt
	 * @param userRequirementsAndPlan user requirements and plan
	 * @param analysisStepsAndData analysis steps and data
	 * @param summaryAndRecommendations summary and recommendations
	 * @return built prompt
	 */
	public static String buildReportGeneratorPromptWithOptimization(String userRequirementsAndPlan,
			String analysisStepsAndData, String summaryAndRecommendations, List<UserPromptConfig> optimizationConfigs, String currDate) {

		Map<String, Object> params = new HashMap<>();
		params.put("user_requirements_and_plan", userRequirementsAndPlan);
		params.put("analysis_steps_and_data", analysisStepsAndData);
		params.put("summary_and_recommendations", summaryAndRecommendations);
		params.put("currDate", currDate);
		params.put("json_example", cleanJsonExample);

		// Build optional optimization section content from user configs
		String optimizationSection = buildOptimizationSection(optimizationConfigs, params);
		params.put("optimization_section", optimizationSection);
		// only plain report
		return PromptConstant.getReportGeneratorPlainPromptTemplate().render(params);
	}

	/**
	 * 构建SQL错误修复提示词
	 * @param sqlGenerationDTO SQL生成参数（包含错误信息）
	 * @return 渲染后的提示词
	 */
	public static String buildSqlErrorFixerPrompt(SqlGenerationDTO sqlGenerationDTO) {
		String schemaInfo = buildMixMacSqlDbPrompt(sqlGenerationDTO.getSchemaDTO(), true);

		Map<String, Object> params = new HashMap<>();
		params.put("dialect", sqlGenerationDTO.getDialect());
		params.put("question", sqlGenerationDTO.getQuery());
		params.put("schema_info", schemaInfo);
		params.put("evidence", sqlGenerationDTO.getEvidence());
		params.put("error_sql", sqlGenerationDTO.getSql());
		params.put("error_message", sqlGenerationDTO.getExceptionMessage());
		params.put("execution_description", sqlGenerationDTO.getExecutionDescription());

		return PromptConstant.getSqlErrorFixerPromptTemplate().render(params);
	}

	/**
	 * 构建业务知识提示词
	 * @param businessTerms 业务术语文本
	 * @return 渲染后的提示词
	 */
	public static String buildBusinessKnowledgePrompt(String businessTerms) {
		Map<String, Object> params = new HashMap<>();
		if (StringUtils.isNotBlank(businessTerms))
			params.put("businessKnowledge", businessTerms);
		else
			params.put("businessKnowledge", "无");
		return PromptConstant.getBusinessKnowledgePromptTemplate().render(params);
	}

	/**
	 * 构建智能体知识提示词
	 * @param agentKnowledge 智能体知识文本
	 * @return 渲染后的提示词
	 */
	// agentKnowledge
	public static String buildAgentKnowledgePrompt(String agentKnowledge) {
		Map<String, Object> params = new HashMap<>();
		if (StringUtils.isNotBlank(agentKnowledge))
			params.put("agentKnowledge", agentKnowledge);
		else
			params.put("agentKnowledge", "无");
		return PromptConstant.getAgentKnowledgePromptTemplate().render(params);
	}

	/**
	 * 构建语义模型提示词
	 * @param semanticModels 语义模型列表
	 * @return 渲染后的提示词
	 */
	public static String buildSemanticModelPrompt(List<SemanticModel> semanticModels) {
		Map<String, Object> params = new HashMap<>();
		String semanticModel = CollUtil.isEmpty(semanticModels) ? ""
				: semanticModels.stream().map(SemanticModel::getPromptInfo).collect(Collectors.joining(";\n"));
		params.put("semanticModel", semanticModel);
		return PromptConstant.getSemanticModelPromptTemplate().render(params);
	}

	/**
	 * 构建优化提示词部分内容
	 * @param optimizationConfigs 优化配置列表
	 * @param params 模板参数
	 * @return 优化部分的内容
	 */
	private static String buildOptimizationSection(List<UserPromptConfig> optimizationConfigs,
			Map<String, Object> params) {

		if (optimizationConfigs == null || optimizationConfigs.isEmpty()) {
			return "";
		}

		StringBuilder result = new StringBuilder();
		result.append("## 优化要求\n");

		for (UserPromptConfig config : optimizationConfigs) {
			String optimizationContent = renderOptimizationPrompt(config.getOptimizationPrompt(), params);
			if (!optimizationContent.trim().isEmpty()) {
				result.append("- ").append(optimizationContent).append("\n");
			}
		}

		return result.toString().trim();
	}

	/**
	 * 构建意图识别提示词
	 * @param multiTurn 多轮对话历史
	 * @param latestQuery 最新用户输入
	 * @return 意图识别提示词
	 */
	public static String buildIntentRecognitionPrompt(String multiTurn, String latestQuery) {
		Map<String, Object> params = new HashMap<>();
		params.put("multi_turn", multiTurn != null ? multiTurn : "(无)");
		params.put("latest_query", latestQuery);
		BeanOutputConverter<IntentRecognitionOutputDTO> beanOutputConverter = new BeanOutputConverter<>(
				IntentRecognitionOutputDTO.class);
		params.put("format", beanOutputConverter.getFormat());
		return PromptConstant.getIntentRecognitionPromptTemplate().render(params);
	}

	/**
	 * 构建查询处理提示词
	 * @param multiTurn 多轮对话历史
	 * @param latestQuery 最新用户输入
	 * @return 查询处理提示词
	 */
	public static String buildQueryEnhancePrompt(String multiTurn, String latestQuery, String evidence) {
		Map<String, Object> params = new HashMap<>();
		params.put("multi_turn", multiTurn != null ? multiTurn : "(无)");
		params.put("latest_query", latestQuery);
		if (StringUtils.isEmpty(evidence))
			params.put("evidence", "无");
		else
			params.put("evidence", evidence);
		params.put("current_time_info", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		BeanOutputConverter<QueryEnhanceOutputDTO> beanOutputConverter = new BeanOutputConverter<>(
				QueryEnhanceOutputDTO.class);
		params.put("format", beanOutputConverter.getFormat());
		return PromptConstant.getQueryEnhancementPromptTemplate().render(params);
	}

	/**
	 * 构建数据视图分析提示词
	 * @return 渲染后的提示词
	 */
	public static String buildDataViewAnalysisPrompt() {
		Map<String, Object> params = new HashMap<>();
		BeanOutputConverter<DisplayStyleBO> beanOutputConverter = new BeanOutputConverter<>(DisplayStyleBO.class);
		params.put("format", beanOutputConverter.getFormat());
		return PromptConstant.getDataViewAnalyzePromptTemplate().render(params);
	}

	/**
	 * 构建可行性评估提示词
	 * @param canonicalQuery 规范化查询
	 * @param recalledSchema 召回的数据库Schema
	 * @param evidence 参考信息
	 * @param multiTurn 多轮对话历史
	 * @return 可行性评估提示词
	 */
	public static String buildFeasibilityAssessmentPrompt(String canonicalQuery, SchemaDTO recalledSchema,
			String evidence, String multiTurn) {
		Map<String, Object> params = new HashMap<>();
		String schemaInfo = buildMixMacSqlDbPrompt(recalledSchema, true);
		params.put("canonical_query", canonicalQuery != null ? canonicalQuery : "");
		params.put("recalled_schema", schemaInfo);
		params.put("evidence", evidence != null ? evidence : "");
		params.put("multi_turn", multiTurn != null ? multiTurn : "(无)");
		return PromptConstant.getFeasibilityAssessmentPromptTemplate().render(params);
	}

	/**
	 * 构建查询重写提示词
	 * @param multiTurn 多轮对话历史
	 * @param latestQuery 最新用户输入
	 * @return 查询重写提示词
	 */
	public static String buildEvidenceQueryRewritePrompt(String multiTurn, String latestQuery) {
		Map<String, Object> params = new HashMap<>();
		params.put("multi_turn", multiTurn != null ? multiTurn : "(无)");
		params.put("latest_query", latestQuery);
		BeanOutputConverter<EvidenceQueryRewriteDTO> beanOutputConverter = new BeanOutputConverter<>(
				EvidenceQueryRewriteDTO.class);
		params.put("format", beanOutputConverter.getFormat());
		return PromptConstant.getEvidenceQueryRewritePromptTemplate().render(params);
	}

	/**
	 * 渲染优化提示词模板
	 * @param optimizationPrompt 优化提示词模板
	 * @param params 参数
	 * @return 渲染后的内容
	 */
	private static String renderOptimizationPrompt(String optimizationPrompt, Map<String, Object> params) {
		if (optimizationPrompt == null || optimizationPrompt.trim().isEmpty()) {
			return "";
		}
		try {
			return new PromptTemplate(optimizationPrompt).render(params);
		}
		catch (Exception e) {
			// 如果模板渲染失败，直接返回原始内容
			return optimizationPrompt;
		}
	}

}
