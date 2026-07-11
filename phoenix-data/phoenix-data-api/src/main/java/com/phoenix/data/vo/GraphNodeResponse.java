package com.phoenix.data.vo;

import com.phoenix.data.enums.TextType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图节点响应对象，用于流式返回节点处理结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GraphNodeResponse {

	private String agentId;

	private String threadId;

	// 使用Constant常量
	private String nodeName;

	private TextType textType;

	private String text;

	@Builder.Default
	private boolean error = false;

	@Builder.Default
	private boolean complete = false;

	/**
	 * 创建错误响应
	 * @param agentId Agent ID
	 * @param threadId 线程ID
	 * @param text 错误文本
	 * @return GraphNodeResponse实例
	 */
	public static GraphNodeResponse error(String agentId, String threadId, String text) {
		return GraphNodeResponse.builder()
			.agentId(agentId)
			.threadId(threadId)
			.text(text)
			.error(true)
			.textType(TextType.TEXT)
			.build();
	}

	/**
	 * 创建完成响应
	 * @param agentId Agent ID
	 * @param threadId 线程ID
	 * @return GraphNodeResponse实例
	 */
	public static GraphNodeResponse complete(String agentId, String threadId) {
		return GraphNodeResponse.builder()
			.agentId(agentId)
			.threadId(threadId)
			.complete(true)
			.textType(TextType.TEXT)
			.build();
	}

}
