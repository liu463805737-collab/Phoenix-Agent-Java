package com.phoenix.data.workflow.dispatcher;

import com.phoenix.data.util.StateUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;

import java.util.Optional;

import static com.phoenix.data.constant.Constant.*;
import static com.alibaba.cloud.ai.graph.StateGraph.END;

/**
 * 表关系分发器，根据表关系处理结果决定下一步节点或重试。
 */
public class TableRelationDispatcher implements EdgeAction {

	/**
	 * 最大重试次数
	 */
	private static final int MAX_RETRY_COUNT = 3;

	/**
	 * 根据表关系处理的异常标志和输出结果决定下一步：
	 * 可重试错误且未超限则重试，否则结束；有正常输出则进入可行性评估节点。
	 *
	 * @param state 全局状态
	 * @return 下一个节点名称
	 */
	@Override
	public String apply(OverAllState state) throws Exception {

		String errorFlag = StateUtil.getStringValue(state, TABLE_RELATION_EXCEPTION_OUTPUT, null);
		Integer retryCount = StateUtil.getObjectValue(state, TABLE_RELATION_RETRY_COUNT, Integer.class, 0);

		if (errorFlag != null && !errorFlag.isEmpty()) {
			if (isRetryableError(errorFlag) && retryCount < MAX_RETRY_COUNT) {
				return TABLE_RELATION_NODE;
			}
			else {
				return END;
			}
		}

		Optional<String> tableRelationOutput = state.value(TABLE_RELATION_OUTPUT);
		if (tableRelationOutput.isPresent()) {
			return FEASIBILITY_ASSESSMENT_NODE;
		}

		// no output, end
		return END;
	}

	/**
	 * 判断错误信息是否可重试（以 "RETRYABLE:" 开头）。
	 *
	 * @param errorMessage 错误信息
	 * @return 是否可重试
	 */
	private boolean isRetryableError(String errorMessage) {
		return errorMessage.startsWith("RETRYABLE:");
	}

}
