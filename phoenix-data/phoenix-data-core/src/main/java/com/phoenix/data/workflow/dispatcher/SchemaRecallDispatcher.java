package com.phoenix.data.workflow.dispatcher;

import com.phoenix.data.util.StateUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;

import java.util.List;

import static com.phoenix.data.constant.Constant.TABLE_DOCUMENTS_FOR_SCHEMA_OUTPUT;
import static com.phoenix.data.constant.Constant.TABLE_RELATION_NODE;
import static com.alibaba.cloud.ai.graph.StateGraph.END;

/**
 * Schema 召回分发器，根据表文档召回结果决定进入表关系节点还是结束流程。
 */
@Slf4j
public class SchemaRecallDispatcher implements EdgeAction {

	/**
	 * 检查是否召回到表文档，若有则进入表关系节点，否则结束。
	 *
	 * @param state 全局状态
	 * @return 下一个节点名称
	 */
	@Override
	public String apply(OverAllState state) throws Exception {
		List<Document> tableDocuments = StateUtil.getDocumentList(state, TABLE_DOCUMENTS_FOR_SCHEMA_OUTPUT);
		if (tableDocuments != null && !tableDocuments.isEmpty())
			return TABLE_RELATION_NODE;
		log.info("No table documents found, ending conversation");
		return END;
	}

}
