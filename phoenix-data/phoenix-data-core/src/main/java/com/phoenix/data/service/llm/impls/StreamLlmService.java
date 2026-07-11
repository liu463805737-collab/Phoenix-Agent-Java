package com.phoenix.data.service.llm.impls;

import com.phoenix.data.service.aimodelconfig.AiModelRegistry;
import com.phoenix.data.service.llm.LlmService;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * 流式 LLM 服务实现，使用流式方式调用大模型并返回响应流。
 */
@AllArgsConstructor
public class StreamLlmService implements LlmService {

	private final AiModelRegistry registry;

	/**
	 * 使用系统提示词和用户提示词流式调用大模型
	 * @param system 系统提示词
	 * @param user 用户提示词
	 * @return ChatResponse 响应流
	 */
	@Override
	public Flux<ChatResponse> call(String system, String user) {
		return registry.getChatClient().prompt().system(system).user(user).stream().chatResponse();
	}

	/**
	 * 仅使用系统提示词流式调用大模型
	 * @param system 系统提示词
	 * @return ChatResponse 响应流
	 */
	@Override
	public Flux<ChatResponse> callSystem(String system) {
		return registry.getChatClient().prompt().system(system).stream().chatResponse();
	}

	/**
	 * 仅使用用户提示词流式调用大模型
	 * @param user 用户提示词
	 * @return ChatResponse 响应流
	 */
	@Override
	public Flux<ChatResponse> callUser(String user) {
		return registry.getChatClient().prompt().user(user).stream().chatResponse();
	}

}
