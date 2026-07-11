package com.phoenix.data.workflow.dispatcher;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;

import static com.phoenix.data.constant.Constant.*;

/**
 * @author zhangshenghang
 */
/**
 * 语义一致性校验分发器，根据校验结果决定进入 SQL 执行节点还是重新生成 SQL。
 */
@Slf4j
public class SemanticConsistenceDispatcher implements EdgeAction {

	/**
	 * 根据语义一致性校验结果分发：通过则执行 SQL，否则重新生成。
	 *
	 * @param state 全局状态
	 * @return 下一个节点名称
	 */
	@Override
	public String apply(OverAllState state) {
		Boolean validate = (Boolean) state.value(SEMANTIC_CONSISTENCY_NODE_OUTPUT).orElse(false);
		log.info("语义一致性校验结果: {}，跳转节点配置", validate);
		if (validate) {
			log.info("语义一致性校验通过，跳转到SQL运行节点。");
			return SQL_EXECUTE_NODE;
		}
		else {
			log.info("语义一致性校验未通过，跳转到SQL生成节点。");
			return SQL_GENERATE_NODE;
		}
	}

}
