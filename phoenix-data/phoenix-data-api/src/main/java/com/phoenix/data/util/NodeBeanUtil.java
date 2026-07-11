package com.phoenix.data.util;

import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 管理Node Bean
 *
 * @author vlsmb
 * @since 2025/9/28
 */
@Component
@AllArgsConstructor
public class NodeBeanUtil {

	private final ApplicationContext context;

	/**
	 * 获取指定类型的NodeAction Bean
	 * @param clazz NodeAction类
	 * @return NodeAction实例
	 */
	public <T extends NodeAction> NodeAction getNodeBean(Class<T> clazz) {
		return context.getBean(clazz);
	}

	/**
	 * 获取指定类型的异步NodeAction
	 * @param clazz NodeAction类
	 * @return 异步NodeAction实例
	 */
	public <T extends NodeAction> AsyncNodeAction getNodeBeanAsync(Class<T> clazz) {
		return AsyncNodeAction.node_async(getNodeBean(clazz));
	}

	/**
	 * 获取指定类型的EdgeAction Bean
	 * @param clazz EdgeAction类
	 * @return EdgeAction实例
	 */
	public <T extends EdgeAction> EdgeAction getEdgeBean(Class<T> clazz) {
		return context.getBean(clazz);
	}

	/**
	 * 获取指定类型的异步EdgeAction
	 * @param clazz EdgeAction类
	 * @return 异步EdgeAction实例
	 */
	public <T extends EdgeAction> AsyncEdgeAction getEdgeBeanAsync(Class<T> clazz) {
		return AsyncEdgeAction.edge_async(getEdgeBean(clazz));
	}

}
