package com.phoenix.data.workflow.dispatcher;

import com.phoenix.data.dto.prompt.QueryEnhanceOutputDTO;
import com.phoenix.data.util.StateUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;

import static com.phoenix.data.constant.Constant.QUERY_ENHANCE_NODE_OUTPUT;
import static com.phoenix.data.constant.Constant.SCHEMA_RECALL_NODE;
import static com.alibaba.cloud.ai.graph.StateGraph.END;

/**
 * 根据查询增强结果决定下一个节点的分发器
 */
@Slf4j
public class QueryEnhanceDispatcher implements EdgeAction {

	/**
	 * 根据查询增强结果分发：结果有效则进入 Schema 召回节点，否则结束。
	 *
	 * @param state 全局状态
	 * @return 下一个节点名称
	 */
	@Override
	public String apply(OverAllState state) throws Exception {
		// 获取查询处理结果
		QueryEnhanceOutputDTO queryProcessOutput = StateUtil.getObjectValue(state, QUERY_ENHANCE_NODE_OUTPUT,
				QueryEnhanceOutputDTO.class);

		// 检查查询处理结果是否为空
		if (queryProcessOutput == null) {
			log.warn("Query process output is null, ending conversation");
			return END;
		}

		// 检查各个字段是否为空
		boolean isCanonicalQueryEmpty = queryProcessOutput.getCanonicalQuery() == null
				|| queryProcessOutput.getCanonicalQuery().trim().isEmpty();
		boolean isExpandedQueriesEmpty = queryProcessOutput.getExpandedQueries() == null
				|| queryProcessOutput.getExpandedQueries().isEmpty();

		if (isCanonicalQueryEmpty || isExpandedQueriesEmpty) {
			log.warn("Query process output contains empty fields - canonicalQuery: {}, expandedQueries: {}",
					isCanonicalQueryEmpty, isExpandedQueriesEmpty);
			return END;
		}
		else {
			log.info("Query process output is valid, proceeding to schema recall");
			return SCHEMA_RECALL_NODE;
		}
	}

}
