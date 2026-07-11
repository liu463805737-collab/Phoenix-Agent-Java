package com.phoenix.data.service.llm;

import com.phoenix.data.util.ChatResponseUtil;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * LLM 服务接口，定义了大模型调用的基本方法，支持同步/阻塞和流式调用模式。
 */
public interface LlmService {

	/**
	 * 调用大模型，传入系统提示词和用户提示词
	 * @param system 系统提示词
	 * @param user 用户提示词
	 * @return ChatResponse 响应流
	 */
	Flux<ChatResponse> call(String system, String user);

	/**
	 * 仅传入系统提示词调用大模型
	 * @param system 系统提示词
	 * @return ChatResponse 响应流
	 */
	Flux<ChatResponse> callSystem(String system);

	/**
	 * 仅传入用户提示词调用大模型
	 * @param user 用户提示词
	 * @return ChatResponse 响应流
	 */
	Flux<ChatResponse> callUser(String user);

	/**
	 * 将响应流阻塞转换为字符串（已废弃）
	 * @deprecated 建议直接使用流式处理
	 * @param responseFlux ChatResponse 响应流
	 * @return 拼接后的字符串
	 */
	@Deprecated
	default String blockToString(Flux<ChatResponse> responseFlux) {
		return toStringFlux(responseFlux).collect(StringBuilder::new, StringBuilder::append)
			.map(StringBuilder::toString)
			.block();
	}

	/**
	 * 将 ChatResponse 响应流转换为文本流
	 * @param responseFlux ChatResponse 响应流
	 * @return 文本字符串流
	 */
	default Flux<String> toStringFlux(Flux<ChatResponse> responseFlux) {
		return responseFlux.map(ChatResponseUtil::getText);
	}

}
