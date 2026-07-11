package com.phoenix.data.workflow.dispatcher;

import com.phoenix.data.dto.datasource.SqlRetryDto;
import com.phoenix.data.util.StateUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;

import static com.phoenix.data.constant.Constant.*;

/**
 * @author zhangshenghang
 */
/**
 * SQL 执行结果分发器，根据执行是否成功决定重新生成 SQL 还是继续执行计划。
 */
@Slf4j
public class SQLExecutorDispatcher implements EdgeAction {

	/**
	 * 根据 SQL 执行结果分发：执行失败则重新生成 SQL，成功则继续执行计划。
	 *
	 * @param state 全局状态
	 * @return 下一个节点名称
	 */
	@Override
	public String apply(OverAllState state) {
		SqlRetryDto retryDto = StateUtil.getObjectValue(state, SQL_REGENERATE_REASON, SqlRetryDto.class);
		if (retryDto.sqlExecuteFail()) {
			log.warn("SQL运行失败，需要重新生成！");
			return SQL_GENERATE_NODE;
		}
		else {
			log.info("SQL运行成功，返回PlanExecutorNode。");
			return PLAN_EXECUTOR_NODE;
		}
	}

}
