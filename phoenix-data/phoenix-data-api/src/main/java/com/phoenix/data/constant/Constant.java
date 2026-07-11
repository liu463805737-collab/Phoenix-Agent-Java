package com.phoenix.data.constant;

/**
 * 系统常量定义类，包含状态图节点名称、状态 key 及事件常量。
 * @author zhangshenghang
 */
public final class Constant {

	/**
	 * 私有构造方法，防止实例化。
	 */
	private Constant() {

	}

	public static final String PROJECT_PROPERTIES_PREFIX = "spring.ai.phoenix.data-agent";

	public static final String INPUT_KEY = "input";

	public static final String AGENT_ID = "agentId";

	public static final String DATASOURCE_ID = "datasourceId";

	public static final String MULTI_TURN_CONTEXT = "MULTI_TURN_CONTEXT";

	public static final String RESULT = "result";

	public static final String NL2SQL_GRAPH_NAME = "nl2sqlGraph";

	public static final String INTENT_RECOGNITION_NODE_OUTPUT = "INTENT_RECOGNITION_NODE_OUTPUT";

	public static final String QUERY_ENHANCE_NODE_OUTPUT = "QUERY_ENHANCE_NODE_OUTPUT";

	public static final String FEASIBILITY_ASSESSMENT_NODE_OUTPUT = "FEASIBILITY_ASSESSMENT_NODE_OUTPUT";

	public static final String EVIDENCE = "EVIDENCE";

	public static final String TABLE_DOCUMENTS_FOR_SCHEMA_OUTPUT = "TABLE_DOCUMENTS_FOR_SCHEMA";

	public static final String SCHEMA_RECALL_NODE_OUTPUT = "SCHEMA_RECALL_NODE_OUTPUT";

	public static final String COLUMN_DOCUMENTS__FOR_SCHEMA_OUTPUT = "COLUMN_DOCUMENTS__FOR_SCHEMA_OUTPUT";

	public static final String TABLE_RELATION_OUTPUT = "TABLE_RELATION_OUTPUT";

	public static final String TABLE_RELATION_EXCEPTION_OUTPUT = "TABLE_RELATION_EXCEPTION_OUTPUT";

	public static final String TABLE_RELATION_RETRY_COUNT = "TABLE_RELATION_RETRY_COUNT";

	public static final String GENEGRATED_SEMANTIC_MODEL_PROMPT = "GENEGRATED_SEMANTIC_MODEL_PROMPT";

	public static final String SQL_GENERATE_OUTPUT = "SQL_GENERATE_OUTPUT";

	public static final String SQL_GENERATE_SCHEMA_MISSING_ADVICE = "SQL_GENERATE_SCHEMA_MISSING_ADVICE";

	public static final String SQL_GENERATE_COUNT = "SQL_GENERATE_COUNT";

	// 重新生成SQL的原因
	public static final String SQL_REGENERATE_REASON = "SQL_REGENERATE_REASON";

	public static final String SEMANTIC_CONSISTENCY_NODE_OUTPUT = "SEMANTIC_CONSISTENCY_NODE_OUTPUT";

	public static final String PLANNER_NODE_OUTPUT = "PLANNER_NODE_OUTPUT";

	public static final String SQL_EXECUTE_NODE_OUTPUT = "SQL_EXECUTE_NODE_OUTPUT";

	// dialect
	public static final String DB_DIALECT_TYPE = "DB_DIALECT_TYPE";

	// Plan当前需要执行的步骤编号
	public static final String PLAN_CURRENT_STEP = "PLAN_CURRENT_STEP";

	// Plan下一个需要进入的节点
	public static final String PLAN_NEXT_NODE = "PLAN_NEXT_NODE";

	// Plan validation
	public static final String PLAN_VALIDATION_STATUS = "PLAN_VALIDATION_STATUS";

	public static final String PLAN_VALIDATION_ERROR = "PLAN_VALIDATION_ERROR";

	public static final String PLAN_REPAIR_COUNT = "PLAN_REPAIR_COUNT";

	// Node KEY
	public static final String PLANNER_NODE = "PLANNER_NODE";

	public static final String PLAN_EXECUTOR_NODE = "PLAN_EXECUTOR_NODE";

	public static final String INTENT_RECOGNITION_NODE = "INTENT_RECOGNITION_NODE";

	public static final String EVIDENCE_RECALL_NODE = "EVIDENCE_RECALL_NODE";

	public static final String QUERY_ENHANCE_NODE = "QUERY_ENHANCE_NODE";

	public static final String FEASIBILITY_ASSESSMENT_NODE = "FEASIBILITY_ASSESSMENT_NODE";

	public static final String REPORT_GENERATOR_NODE = "REPORT_GENERATOR_NODE";

	public static final String SCHEMA_RECALL_NODE = "SCHEMA_RECALL_NODE";

	public static final String TABLE_RELATION_NODE = "TABLE_RELATION_NODE";

	public static final String SQL_GENERATE_NODE = "SQL_GENERATE_NODE";

	public static final String SQL_EXECUTE_NODE = "SQL_EXECUTE_NODE";

	public static final String SEMANTIC_CONSISTENCY_NODE = "SEMANTIC_CONSISTENCY_NODE";

	public static final String HUMAN_FEEDBACK_NODE = "HUMAN_FEEDBACK_NODE";

	// Keys related to Python code execution
	public static final String PYTHON_GENERATE_NODE = "PYTHON_GENERATE_NODE";

	public static final String PYTHON_EXECUTE_NODE = "PYTHON_EXECUTE_NODE";

	public static final String PYTHON_ANALYZE_NODE = "PYTHON_ANALYZE_NODE";

	public static final String SQL_RESULT_LIST_MEMORY = "SQL_RESULT_LIST_MEMORY";

	public static final String PYTHON_IS_SUCCESS = "PYTHON_IS_SUCCESS";

	public static final String PYTHON_TRIES_COUNT = "PYTHON_TRIES_COUNT";

	// 标记是否进入Python执行失败的降级模式（超过最大重试次数后触发）
	public static final String PYTHON_FALLBACK_MODE = "PYTHON_FALLBACK_MODE";

	// If code execution succeeds, output code running result; if fails, output error
	// information
	public static final String PYTHON_EXECUTE_NODE_OUTPUT = "PYTHON_EXECUTE_NODE_OUTPUT";

	public static final String PYTHON_GENERATE_NODE_OUTPUT = "PYTHON_GENERATE_NODE_OUTPUT";

	public static final String PYTHON_ANALYSIS_NODE_OUTPUT = "PYTHON_ANALYSIS_NODE_OUTPUT";

	// nl2sql接口预留相关
	public static final String IS_ONLY_NL2SQL = "IS_ONLY_NL2SQL";

	// 人类复核相关
	public static final String HUMAN_REVIEW_ENABLED = "HUMAN_REVIEW_ENABLED";

	// Human feedback data payload
	public static final String HUMAN_FEEDBACK_DATA = "HUMAN_FEEDBACK_DATA";

	// StreamEvent 常量
	public static final String STREAM_EVENT_COMPLETE = "complete";

	public static final String STREAM_EVENT_ERROR = "error";

	// Langfuse 追踪：threadId 透传到 graph state，用于 token 累计
	public static final String TRACE_THREAD_ID = "TRACE_THREAD_ID";
	public static final String FRONT_LOGIN_INFO = "FRONT_LOGIN_INFO";

}
