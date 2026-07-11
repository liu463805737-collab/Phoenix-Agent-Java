package com.phoenix.data.util;

import com.phoenix.data.enums.TextType;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

/**
 * @author zhangshenghang
 */
public class ChatResponseUtil {

	/**
	 * 创建带换行的状态响应消息
	 * @param statusMessage 状态消息
	 * @return ChatResponse 对象
	 */
	public static ChatResponse createResponse(String statusMessage) {
		return createPureResponse(statusMessage + "\n");
	}

	/**
	 * 创建纯文本响应消息
	 * @param message 消息内容
	 * @return ChatResponse 对象
	 */
	public static ChatResponse createPureResponse(String message) {
		AssistantMessage assistantMessage = new AssistantMessage(message);
		Generation generation = new Generation(assistantMessage);
		return new ChatResponse(List.of(generation));
	}

	/**
	 * 创建去除标记符号的响应消息（已弃用，效果不佳）
	 * @param message 原始消息
	 * @param textType 文本类型
	 * @return ChatResponse 对象
	 */
	@Deprecated
	public static ChatResponse createTrimResponse(String message, TextType textType) {
		return createPureResponse(message.replace(textType.getStartSign(), "").replace(textType.getEndSign(), ""));
	}

	/**
	 * 从 ChatResponse 中提取文本内容
	 * @param chatResponse ChatResponse 对象
	 * @return 提取的文本
	 */
	public static String getText(ChatResponse chatResponse) {
		Generation result = chatResponse.getResult();
		if (result == null) {
			return "";
		}
		AssistantMessage output = result.getOutput();
		if (output == null) {
			return "";
		}
		return output.getText() == null ? "" : output.getText();
	}

}
