package com.phoenix.data.workflow.dispatcher;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;

import static com.phoenix.data.constant.Constant.FEASIBILITY_ASSESSMENT_NODE_OUTPUT;
import static com.phoenix.data.constant.Constant.PLANNER_NODE;
import static com.alibaba.cloud.ai.graph.StateGraph.END;

/**
 * 可行性评估分发器，根据可行性评估结果决定下一步进入 PlannerNode 还是直接结束。
 */
@Slf4j
public class FeasibilityAssessmentDispatcher implements EdgeAction {

	/**
	 * 根据可行性评估节点的输出判断需求类型，
	 * 若为数据分析则进入 PlannerNode，否则结束流程。
	 *
	 * @param state 全局状态
	 * @return 下一个节点名称
	 */
	@Override
	public String apply(OverAllState state) throws Exception {
		// value的值是和 resources/feasibility-assessment.txt的输出一致，例如
		// 【需求类型】：《数据分析》
		// 【语种类型】：《中文》
		// 【需求内容】：查询所有“核心用户”的数量
		String value = state.value(FEASIBILITY_ASSESSMENT_NODE_OUTPUT, END);

		if (value != null && value.contains("【需求类型】：《数据分析》")) {
			log.info("[FeasibilityAssessmentNodeDispatcher]需求类型为数据分析，进入PlannerNode节点");
			return PLANNER_NODE;
		}
		else {
			log.info("[FeasibilityAssessmentNodeDispatcher]需求类型非数据分析，返回END节点");
			return END;
		}
	}

}
