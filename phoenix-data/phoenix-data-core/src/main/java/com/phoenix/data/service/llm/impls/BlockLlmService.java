package com.phoenix.data.service.llm.impls;

import com.phoenix.data.service.aimodelconfig.AiModelRegistry;
import com.phoenix.data.service.llm.LlmService;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 阻塞式 LLM 服务实现，使用同步方式调用大模型并返回响应。
 */
@AllArgsConstructor
public class BlockLlmService implements LlmService {

	private final AiModelRegistry registry;

	/**
	 * 使用系统提示词和用户提示词同步调用大模型
	 * @param system 系统提示词
	 * @param user 用户提示词
	 * @return ChatResponse 响应流
	 */
	@Override
	public Flux<ChatResponse> call(String system, String user) {
		return Mono
			.fromCallable(() -> registry.getChatClient().prompt().system(system).user(user).call().chatResponse())
			.flux();
	}

	/**
	 * 仅使用系统提示词同步调用大模型
	 * @param system 系统提示词
	 * @return ChatResponse 响应流
	 */
	@Override
	public Flux<ChatResponse> callSystem(String system) {
		return Mono.fromCallable(() -> registry.getChatClient().prompt().system(system).call().chatResponse()).flux();
	}

	/**
	 * 仅使用用户提示词同步调用大模型
	 * @param user 用户提示词
	 * @return ChatResponse 响应流
	 */
	@Override
	public Flux<ChatResponse> callUser(String user) {
		return Mono.fromCallable(() -> registry.getChatClient().prompt().user(user).call().chatResponse()).flux();
	}

}
