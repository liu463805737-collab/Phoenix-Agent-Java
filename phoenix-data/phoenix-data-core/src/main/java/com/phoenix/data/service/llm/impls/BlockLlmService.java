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
public class BlockLlmService extends AbstractLlmService implements LlmService {

	public BlockLlmService(AiModelRegistry registry) {
		super(registry);
	}

	/**
	 * 使用系统提示词和用户提示词同步调用大模型
	 * @param system 系统提示词
	 * @param user 用户提示词
	 * @return ChatResponse 响应流
	 */
	@Override
	public Flux<ChatResponse> call(String system, String user) {
		return Mono
			.fromCallable(() -> this.getChatClientRequestSpec(system, user).call().chatResponse())
			.flux();
	}

	/**
	 * 仅使用系统提示词同步调用大模型
	 * @param system 系统提示词
	 * @return ChatResponse 响应流
	 */
	@Override
	public Flux<ChatResponse> callSystem(String system) {
		return Mono.fromCallable(() -> this.getChatClientRequestSpec(system, null).call().chatResponse()).flux();
	}

	/**
	 * 仅使用用户提示词同步调用大模型
	 * @param user 用户提示词
	 * @return ChatResponse 响应流
	 */
	@Override
	public Flux<ChatResponse> callUser(String user) {
		return Mono.fromCallable(() -> this.getChatClientRequestSpec(null, user).call().chatResponse()).flux();
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
		return Mono.fromCallable(() -> this.getChatClientRequestSpec(system, user, maxOutputTokens)
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
		return Mono.fromCallable(() -> this.getChatClientRequestSpec(system, null, maxOutputTokens)
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
		return Mono.fromCallable(() -> this.getChatClientRequestSpec(null, user, maxOutputTokens)
			.call()
			.chatResponse()).flux();
	}

}
