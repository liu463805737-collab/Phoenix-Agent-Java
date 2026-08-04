package com.phoenix.data.service.llm.impls;

import com.phoenix.data.service.aimodelconfig.AiModelRegistry;
import com.phoenix.data.service.llm.AbstractLlmService;
import com.phoenix.data.service.llm.LlmService;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import reactor.core.publisher.Flux;

/**
 * 流式 LLM 服务实现，使用流式方式调用大模型并返回响应流。
 */
public class StreamLlmService extends AbstractLlmService implements LlmService {


    public StreamLlmService(AiModelRegistry registry) {
        super(registry);
    }

    /**
     * 使用系统提示词和用户提示词流式调用大模型
     *
     * @param system 系统提示词
     * @param user   用户提示词
     * @return ChatResponse 响应流
     */
    @Override
    public Flux<ChatResponse> call(String system, String user) {
        return this.getChatClientRequestSpec(system, user).stream().chatResponse();
    }

    /**
     * 仅使用系统提示词流式调用大模型
     *
     * @param system 系统提示词
     * @return ChatResponse 响应流
     */
    @Override
    public Flux<ChatResponse> callSystem(String system) {
        return this.getChatClientRequestSpec(system, null).stream().chatResponse();
    }

    /**
     * 仅使用用户提示词流式调用大模型
     *
     * @param user 用户提示词
     * @return ChatResponse 响应流
     */
    @Override
    public Flux<ChatResponse> callUser(String user) {
        return this.getChatClientRequestSpec(null, user).stream().chatResponse();
    }

    /**
     * 使用系统提示词和用户提示词流式调用大模型，指定最大输出 token 数
     *
     * @param system          系统提示词
     * @param user            用户提示词
     * @param maxOutputTokens 最大输出 token 数
     * @return ChatResponse 响应流
     */
    @Override
    public Flux<ChatResponse> call(String system, String user, int maxOutputTokens) {
        return this.getChatClientRequestSpec(system, user, maxOutputTokens).stream().chatResponse();
    }

    /**
     * 仅使用系统提示词流式调用大模型，指定最大输出 token 数
     *
     * @param system          系统提示词
     * @param maxOutputTokens 最大输出 token 数
     * @return ChatResponse 响应流
     */
    @Override
    public Flux<ChatResponse> callSystem(String system, int maxOutputTokens) {
        return  this.getChatClientRequestSpec(system, null, maxOutputTokens)
                .stream()
                .chatResponse();
    }

    /**
     * 仅使用用户提示词流式调用大模型，指定最大输出 token 数
     *
     * @param user            用户提示词
     * @param maxOutputTokens 最大输出 token 数
     * @return ChatResponse 响应流
     */
    @Override
    public Flux<ChatResponse> callUser(String user, int maxOutputTokens) {
        return this.getChatClientRequestSpec(null, user, maxOutputTokens)
                .stream()
                .chatResponse();
    }

}
