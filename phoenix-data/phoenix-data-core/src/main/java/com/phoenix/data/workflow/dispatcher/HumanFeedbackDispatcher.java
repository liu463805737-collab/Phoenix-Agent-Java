package com.phoenix.data.workflow.dispatcher;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;

import static com.phoenix.data.constant.Constant.HUMAN_FEEDBACK_NODE;
import static com.alibaba.cloud.ai.graph.StateGraph.END;

/**
 * Dispatcher for human feedback node routing.
 *
 * @author Makoto
 */
public class HumanFeedbackDispatcher implements EdgeAction {

	/**
	 * 根据人工反馈状态决定下一步节点。
	 * 如果等待反馈则停留在当前节点，否则跳转到指定节点。
	 *
	 * @param state 全局状态
	 * @return 下一个节点名称
	 */
	@Override
	public String apply(OverAllState state) throws Exception {
		String nextNode = (String) state.value("human_next_node", END);

		// 如果是等待反馈状态，返回END让图暂停
		if ("WAIT_FOR_FEEDBACK".equals(nextNode)) {
			return HUMAN_FEEDBACK_NODE;
		}

		return nextNode;
	}

}
