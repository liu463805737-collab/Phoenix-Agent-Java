package com.phoenix.data.workflow.dispatcher;

import com.phoenix.data.properties.CodeExecutorProperties;
import com.phoenix.data.util.StateUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;

import static com.phoenix.data.constant.Constant.*;
import static com.alibaba.cloud.ai.graph.StateGraph.END;

/**
 * @author vlsmb
 * @since 2025/7/29
 */
@Slf4j
public class PythonExecutorDispatcher implements EdgeAction {

	private final CodeExecutorProperties codeExecutorProperties;

	/**
	 * 构造 Python 执行分发器。
	 *
	 * @param codeExecutorProperties 代码执行器配置属性
	 */
	public PythonExecutorDispatcher(CodeExecutorProperties codeExecutorProperties) {
		this.codeExecutorProperties = codeExecutorProperties;
	}

	/**
	 * 根据 Python 执行结果分发：成功则进入分析节点，失败则重试生成或结束。
	 *
	 * @param state 全局状态
	 * @return 下一个节点名称
	 */
	@Override
	public String apply(OverAllState state) throws Exception {
		boolean isFallbackMode = StateUtil.getObjectValue(state, PYTHON_FALLBACK_MODE, Boolean.class, false);
		if (isFallbackMode) {
			log.warn("Python执行进入降级模式，跳过重试直接进入分析节点");
			return PYTHON_ANALYZE_NODE;
		}

		// Determine if failed
		boolean isSuccess = StateUtil.getObjectValue(state, PYTHON_IS_SUCCESS, Boolean.class, false);
		if (!isSuccess) {
			String message = StateUtil.getStringValue(state, PYTHON_EXECUTE_NODE_OUTPUT);
			log.error("Python Executor Node Error: {}", message);
			int tries = StateUtil.getObjectValue(state, PYTHON_TRIES_COUNT, Integer.class, 0);
			if (tries >= codeExecutorProperties.getPythonMaxTriesCount()) {
				log.error("Python执行失败且已超过最大重试次数（已尝试次数：{}），流程终止", tries);
				return END;
			}
			else {
				// Regenerate code for testing
				return PYTHON_GENERATE_NODE;
			}
		}
		// Go to code execution result analysis node
		return PYTHON_ANALYZE_NODE;
	}

}
