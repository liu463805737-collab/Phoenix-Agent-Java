package com.phoenix.data.service.vectorstore;

import com.phoenix.data.constant.Constant;
import com.phoenix.data.constant.DocumentMetadataConstant;
import com.phoenix.data.mapper.AgentKnowledgeMapper;
import com.phoenix.data.mapper.BusinessKnowledgeMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 动态过滤服务，构建向量存储的过滤表达式，支持按智能体、向量类型和启用状态进行过滤。
 */
@Slf4j
@Component
@AllArgsConstructor
public class DynamicFilterService {

	private final AgentKnowledgeMapper agentKnowledgeMapper;

	private final BusinessKnowledgeMapper businessKnowledgeMapper;

	/**
	 * 构建动态过滤表达式，根据向量类型自动添加启用状态和 ID 过滤条件
	 * @param agentId 智能体 ID
	 * @param vectorType 向量类型
	 * @return 过滤表达式，无有效数据时返回 null
	 */
	public Filter.Expression buildDynamicFilter(String agentId, String vectorType) {
		FilterExpressionBuilder b = new FilterExpressionBuilder();
		List<Filter.Expression> conditions = new ArrayList<>();

		// 必须条件
		conditions.add(b.eq(Constant.AGENT_ID, agentId).build());
		conditions.add(b.eq(DocumentMetadataConstant.VECTOR_TYPE, vectorType).build());

		switch (vectorType) {

			case DocumentMetadataConstant.AGENT_KNOWLEDGE:
				// 场景 A: 知识库文档 -> 需要查 MySQL 获取启用状态
				List<Integer> validIds = agentKnowledgeMapper.selectRecalledKnowledgeIds(Integer.valueOf(agentId));

				if (validIds.isEmpty()) {
					log.warn("Agent {} has no recalled knowledge documents. Returning empty filter signal.", agentId);
					return null;
				}
				else {
					// 加入 ID 过滤
					conditions.add(b.in(DocumentMetadataConstant.DB_AGENT_KNOWLEDGE_ID, validIds.toArray()).build());
				}
				break;

			case DocumentMetadataConstant.BUSINESS_TERM:
				// 场景 B: 业务知识 -> 查 business_knowledge 表的需要召回的
				List<Long> recalledBusinessKnowledgeIds = businessKnowledgeMapper
					.selectRecalledKnowledgeIds(Long.valueOf(agentId));

				if (recalledBusinessKnowledgeIds.isEmpty()) {
					log.warn("Agent {} has no recalled business terms. Returning empty filter signal.", agentId);
					return null;
				}
				else {
					// 添加 ID 过滤
					conditions
						.add(b.in(DocumentMetadataConstant.DB_BUSINESS_TERM_ID, recalledBusinessKnowledgeIds.toArray())
							.build());
				}
				break;

			default:
				// 其他类型，默认只用 agentId + vectorType 过滤，不做额外处理
				log.debug("Using default filter for type: {}", vectorType);
				break;
		}

		// 组合所有条件
		return combineWithAnd(conditions);
	}

	/**
	 * 将多个过滤条件用 AND 连接起来
	 * @param conditions 条件列表
	 * @return 组合后的 Expression
	 */
	public static Filter.Expression combineWithAnd(List<Filter.Expression> conditions) {
		// 1. 判空
		if (conditions == null || conditions.isEmpty()) {
			return null;
		}

		// 2. 如果只有一个条件，直接返回，不用拼 AND
		if (conditions.size() == 1) {
			return conditions.get(0);
		}

		// 3. 核心逻辑：循环两两拼接
		// 初始结果是第一个条件
		Filter.Expression result = conditions.get(0);

		// 从第二个条件开始遍历
		for (int i = 1; i < conditions.size(); i++) {
			Filter.Expression nextCondition = conditions.get(i);

			// 手动创建 Expression 对象
			// 结构：(Result AND Next)
			result = new Filter.Expression(Filter.ExpressionType.AND, // 指定操作符
					result, // 左节点 (累加的结果)
					nextCondition // 右节点 (当前条件)
			);
		}

		return result;
	}

	/**
	 * 将元数据 Map 转换为过滤表达式字符串
	 * @param filterMap 元数据键值对
	 * @return 过滤表达式字符串
	 */
	public static String buildFilterExpressionString(Map<String, Object> filterMap) {
		if (filterMap == null || filterMap.isEmpty()) {
			return null;
		}

		// 验证键名是否合法（只包含字母、数字和下划线）
		for (String key : filterMap.keySet()) {
			if (!key.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
				throw new IllegalArgumentException("Invalid key name: " + key
						+ ". Keys must start with a letter or underscore and contain only alphanumeric characters and underscores.");
			}
		}

		return filterMap.entrySet().stream().map(entry -> {
			String key = entry.getKey();
			Object value = entry.getValue();

			// 处理空值
			if (value == null) {
				return key + " == null";
			}

			// 根据值的类型决定如何格式化
			if (value instanceof String) {
				// 转义字符串中的特殊字符
				String escapedValue = escapeStringLiteral((String) value);
				return key + " == '" + escapedValue + "'";
			}
			else if (value instanceof Number) {
				// 数字类型直接使用
				return key + " == " + value;
			}
			else if (value instanceof Boolean) {
				// 布尔值使用小写形式
				return key + " == " + ((Boolean) value).toString().toLowerCase();
			}
			else if (value instanceof Enum) {
				// 枚举类型，转换为字符串并转义
				String enumValue = ((Enum<?>) value).name();
				String escapedValue = escapeStringLiteral(enumValue);
				return key + " == '" + escapedValue + "'";
			}
			else {
				// 其他类型尝试转换为字符串并转义
				String stringValue = value.toString();
				String escapedValue = escapeStringLiteral(stringValue);
				return key + " == '" + escapedValue + "'";
			}
		}).collect(Collectors.joining(" && "));
	}

	/**
	 * 转义字符串字面量中的特殊字符
	 */
	public static String escapeStringLiteral(String input) {
		if (input == null) {
			return "";
		}

		// 转义反斜杠和单引号
		String escaped = input.replace("\\", "\\\\").replace("'", "\\'");

		// 转义其他特殊字符
		escaped = escaped.replace("\n", "\\n")
			.replace("\r", "\\r")
			.replace("\t", "\\t")
			.replace("\b", "\\b")
			.replace("\f", "\\f");

		return escaped;
	}

	/**
	 * 构建用于搜索表的过滤表达式
	 * @param datasourceId 数据源 ID
	 * @param tableNames 表名列表
	 * @return 过滤表达式
	 */
	public static Filter.Expression buildFilterExpressionForSearchTables(Integer datasourceId,Long agentId,
			List<String> tableNames) {
		FilterExpressionBuilder b = new FilterExpressionBuilder();
		List<Filter.Expression> conditions = new ArrayList<>();

		// 1. 基础条件：datasourceId
		conditions.add(b.eq(Constant.DATASOURCE_ID, datasourceId.toString()).build());
		conditions.add(b.eq(Constant.AGENT_ID, agentId.toString()).build());

		// 2. 基础条件：vectorType = TABLE
		conditions.add(b.eq(DocumentMetadataConstant.VECTOR_TYPE, DocumentMetadataConstant.TABLE).build());

		// 3. 动态条件：表名列表 IN 查询
		if (tableNames != null && !tableNames.isEmpty()) {
			conditions.add(b.in(DocumentMetadataConstant.NAME, tableNames.toArray()).build());
		}
		else {
			log.warn("Table names list is empty. Returning empty filter signal.");
			return null;
		}
		return combineWithAnd(conditions);
	}

	/**
	 * 构建用于搜索表的过滤表达式
	 * @param datasourceId 数据源 ID
	 * @param agentId 智能体id
	 * @return 过滤表达式
	 */
	public static Filter.Expression buildFilterExpressionForAgentDb(Integer datasourceId,
	                                                                     Long agentId) {
		FilterExpressionBuilder b = new FilterExpressionBuilder();
		List<Filter.Expression> conditions = new ArrayList<>();
		// 1. 基础条件：datasourceId
		conditions.add(b.eq(Constant.DATASOURCE_ID, datasourceId.toString()).build());
		conditions.add(b.eq(Constant.AGENT_ID, agentId.toString()).build());
		return combineWithAnd(conditions);
	}

	/**
	 * 构建用于搜索列的过滤表达式
	 * @param datasourceId 数据源 ID
	 * @param upstreamTableNames 上游表名列表
	 * @return 过滤表达式
	 */
	public Filter.Expression buildFilterExpressionForSearchColumns(Integer datasourceId,Long agentId,
			List<String> upstreamTableNames) {
		if (upstreamTableNames == null || upstreamTableNames.isEmpty()) {
			log.warn("Upstream table names list is empty. Returning empty filter signal.");
			return null;
		}

		FilterExpressionBuilder b = new FilterExpressionBuilder();
		List<Filter.Expression> conditions = new ArrayList<>();

		// 1. DatasourceId 条件
		conditions.add(b.eq(Constant.DATASOURCE_ID, datasourceId.toString()).build());
		conditions.add(b.eq(Constant.AGENT_ID, agentId.toString()).build());

		// 2. VectorType 条件
		conditions.add(b.eq(DocumentMetadataConstant.VECTOR_TYPE, DocumentMetadataConstant.COLUMN).build());

		// 3. TableName 条件
		conditions.add(b.in(DocumentMetadataConstant.TABLE_NAME, upstreamTableNames.toArray()).build());

		return combineWithAnd(conditions);
	}

}
