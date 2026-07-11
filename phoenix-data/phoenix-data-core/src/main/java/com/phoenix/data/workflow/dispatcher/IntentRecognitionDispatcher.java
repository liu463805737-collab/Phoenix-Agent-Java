package com.phoenix.data.workflow.dispatcher;

import com.phoenix.data.dto.prompt.IntentRecognitionOutputDTO;
import com.phoenix.data.util.StateUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;

import static com.phoenix.data.constant.Constant.EVIDENCE_RECALL_NODE;
import static com.phoenix.data.constant.Constant.INTENT_RECOGNITION_NODE_OUTPUT;
import static com.alibaba.cloud.ai.graph.StateGraph.END;

/**
 * 根据意图识别结果决定下一个节点的分发器
 */
@Slf4j
public class IntentRecognitionDispatcher implements EdgeAction {

	/**
	 * 根据意图识别结果分发：闲聊或无关指令则结束，数据分析请求则进入证据召回节点。
	 *
	 * @param state 全局状态
	 * @return 下一个节点名称
	 */
	@Override
	public String apply(OverAllState state) throws Exception {
		// 获取意图识别结果
		IntentRecognitionOutputDTO intentResult = StateUtil.getObjectValue(state, INTENT_RECOGNITION_NODE_OUTPUT,
				IntentRecognitionOutputDTO.class);

		if (intentResult == null || intentResult.getClassification() == null
				|| intentResult.getClassification().trim().isEmpty()) {
			log.warn("Intent recognition result is null or empty, defaulting to END");
			return END;
		}

		String classification = intentResult.getClassification();

		// 根据分类结果决定下一个节点
		if ("《闲聊或无关指令》".equals(classification)) {
			log.warn("Intent classified as chat or irrelevant, ending conversation");
			return END;
		}
		else {
			log.info("Intent classified as potential data analysis request, proceeding to evidence recall");
			return EVIDENCE_RECALL_NODE;
		}
	}

}
