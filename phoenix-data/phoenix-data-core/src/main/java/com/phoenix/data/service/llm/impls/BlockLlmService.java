package com.phoenix.data.service.llm.impls;

import com.phoenix.data.service.aimodelconfig.AiModelRegistry;
import com.phoenix.data.service.llm.AbstractLlmService;
import com.phoenix.data.service.llm.LlmService;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 阻塞式 LLM 服务实现，使用同步方式调用大模型并返回响应。
 */
@AllArgsConstructor
public class BlockLlmService extends AbstractLlmService implements LlmService {

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
			.fromCallable(() -> registry.getChatClient().prompt().options(this.setMaxtokens(system, user)).system(system).user(user).call().chatResponse())
			.flux();
	}

	/**
	 * 仅使用系统提示词同步调用大模型
	 * @param system 系统提示词
	 * @return ChatResponse 响应流
	 */
	@Override
	public Flux<ChatResponse> callSystem(String system) {
		return Mono.fromCallable(() -> registry.getChatClient().prompt().options(this.setMaxtokens(system, null)).system(system).call().chatResponse()).flux();
	}

	/**
	 * 仅使用用户提示词同步调用大模型
	 * @param user 用户提示词
	 * @return ChatResponse 响应流
	 */
	@Override
	public Flux<ChatResponse> callUser(String user) {
		return Mono.fromCallable(() -> registry.getChatClient().prompt().options(this.setMaxtokens(null, user)).user(user).call().chatResponse()).flux();
	}

	/**
	 * 使用系统提示词和用户提示词同步调用大模型，指定最大输出 token 数
	 * @param system 系统提示词
	 * @param user 用户提示词
	 * @param maxOutputTokens 最大输出 token 数
	 * @return ChatResponse 响应流
	 */
	@Override
	public Flux<ChatResponse> call(String system, String user, int maxOutputTokens) {
		return Mono.fromCallable(() -> registry.getChatClient().prompt()
			.options(OpenAiChatOptions.builder().maxTokens(maxOutputTokens).build())
			.system(system)
			.user(user)
			.call()
			.chatResponse()).flux();
	}

	/**
	 * 仅使用系统提示词同步调用大模型，指定最大输出 token 数
	 * @param system 系统提示词
	 * @param maxOutputTokens 最大输出 token 数
	 * @return ChatResponse 响应流
	 */
	@Override
	public Flux<ChatResponse> callSystem(String system, int maxOutputTokens) {
		return Mono.fromCallable(() -> registry.getChatClient().prompt()
			.options(OpenAiChatOptions.builder().maxTokens(maxOutputTokens).build())
			.system(system)
			.call()
			.chatResponse()).flux();
	}

	/**
	 * 仅使用用户提示词同步调用大模型，指定最大输出 token 数
	 * @param user 用户提示词
	 * @param maxOutputTokens 最大输出 token 数
	 * @return ChatResponse 响应流
	 */
	@Override
	public Flux<ChatResponse> callUser(String user, int maxOutputTokens) {
		return Mono.fromCallable(() -> registry.getChatClient().prompt()
			.options(OpenAiChatOptions.builder().maxTokens(maxOutputTokens).build())
			.user(user)
			.call()
			.chatResponse()).flux();
	}

}
