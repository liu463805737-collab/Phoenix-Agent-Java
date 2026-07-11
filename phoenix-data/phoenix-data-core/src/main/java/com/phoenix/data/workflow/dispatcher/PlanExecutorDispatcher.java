package com.phoenix.data.workflow.dispatcher;

import com.phoenix.data.util.StateUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;

import static com.phoenix.data.constant.Constant.*;
import static com.alibaba.cloud.ai.graph.StateGraph.END;

/**
 * Dispatches to the next node based on the plan execution and validation status.
 *
 * @author zhangshenghang
 */
@Slf4j
public class PlanExecutorDispatcher implements EdgeAction {

	/**
	 * 最大计划修复尝试次数
	 */
	private static final int MAX_REPAIR_ATTEMPTS = 2;

	/**
	 * 根据计划校验结果分发：校验通过则继续下一步，失败则返回 PlannerNode 重试或结束。
	 *
	 * @param state 全局状态
	 * @return 下一个节点名称
	 */
	@Override
	public String apply(OverAllState state) {
		boolean validationPassed = StateUtil.getObjectValue(state, PLAN_VALIDATION_STATUS, Boolean.class, false);

		if (validationPassed) {
			log.info("Plan validation passed. Proceeding to next step.");
			String nextNode = state.value(PLAN_NEXT_NODE, END);
			// 如果返回的是"END"，直接返回END常量
			if ("END".equals(nextNode)) {
				log.info("Plan execution completed successfully.");
				return END;
			}
			return nextNode;
		}
		else {
			// Plan validation failed, check repair count and decide whether to retry or
			// end.
			int repairCount = StateUtil.getObjectValue(state, PLAN_REPAIR_COUNT, Integer.class, 0);

			if (repairCount > MAX_REPAIR_ATTEMPTS) {
				log.error("Plan repair attempts exceeded the limit of {}. Terminating execution.", MAX_REPAIR_ATTEMPTS);
				// The node is responsible for setting the final error message.
				return END;
			}

			log.warn("Plan validation failed. Routing back to PlannerNode for repair. Attempt count from state: {}.",
					repairCount);
			return PLANNER_NODE;
		}
	}

}
