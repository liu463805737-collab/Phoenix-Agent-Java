package com.phoenix.data.workflow.dispatcher;

import com.phoenix.data.properties.DataAgentProperties;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.phoenix.data.constant.Constant.*;
import static com.alibaba.cloud.ai.graph.StateGraph.END;

/**
 * @author zhangshenghang
 */
@Slf4j
@Component
@AllArgsConstructor
public class SqlGenerateDispatcher implements EdgeAction {

	private final DataAgentProperties properties;

	/**
	 * 根据 SQL 生成结果分发：失败则重试或结束，成功则进入语义一致性检查节点。
	 *
	 * @param state 全局状态
	 * @return 下一个节点名称
	 */
	@Override
	public String apply(OverAllState state) {
		Optional<Object> optional = state.value(SQL_GENERATE_OUTPUT);
		if (optional.isEmpty()) {
			int currentCount = state.value(SQL_GENERATE_COUNT, properties.getMaxSqlRetryCount());
			// 生成失败，重新生成
			if (currentCount < properties.getMaxSqlRetryCount()) {
				log.info("SQL 生成失败，开始重试，当前次数: {}", currentCount);
				return SQL_GENERATE_NODE;
			}
			log.error("SQL 生成失败，达到最大重试次数，结束流程");
			return END;
		}
		String sqlGenerateOutput = (String) optional.get();
		log.info("SQL 生成结果: {}", sqlGenerateOutput);

		if (END.equals(sqlGenerateOutput)) {
			log.info("检测到流程结束标志: {}", END);
			return END;
		}
		else {
			log.info("SQL生成成功，进入语义一致性检查节点: {}", SEMANTIC_CONSISTENCY_NODE);
			return SEMANTIC_CONSISTENCY_NODE;
		}
	}

}
