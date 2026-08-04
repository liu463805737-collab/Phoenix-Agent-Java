package com.phoenix.data.service.llm;

import com.phoenix.data.service.aimodelconfig.AiModelRegistry;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;

@AllArgsConstructor
public abstract class AbstractLlmService {
    protected final AiModelRegistry registry;

    /**
     * 获取最大的token
     *
     * @param system 系统提示词
     * @param user   用户提示词
     * @return
     */
    protected Integer maxToken(String system, String user) {
        Integer maxTokens = 4096;
        StringBuilder fullPrompt = new StringBuilder("");
        if (StringUtils.isNotBlank(user)) {
            fullPrompt.append(user);
        }
        if (StringUtils.isNotBlank(system)) {
            fullPrompt.append(system);
        }
        if (fullPrompt.length() > 0) {
            maxTokens = fullPrompt.length() * 2;
        }
        if (maxTokens > 50000) {
            maxTokens = 30000;
        }
        return maxTokens;
    }

    protected OpenAiChatOptions setMaxtokens(String system, String user) {
        Integer maxTokens = maxToken(system, user);
        return OpenAiChatOptions.builder().maxTokens(maxTokens).build();
    }


    protected ChatClient.ChatClientRequestSpec getChatClientRequestSpec(String system, String user) {
        ChatClient.ChatClientRequestSpec options = registry.getChatClient().prompt().options(this.setMaxtokens(system, user));
        if (StringUtils.isNotBlank(user)) {
            options.user(user);
        }
        if (StringUtils.isNotBlank(system)) {
            options.system(system);
        }
        return options;
    }

    protected ChatClient.ChatClientRequestSpec getChatClientRequestSpec(String system, String user, Integer maxTokens) {
        ChatClient.ChatClientRequestSpec options = registry.getChatClient().prompt();
        if (StringUtils.isNotBlank(user)) {
            options.user(user);
        }
        if (StringUtils.isNotBlank(system)) {
            options.system(system);
        }
        if (maxTokens != null) {
            options.options(OpenAiChatOptions.builder().maxTokens(maxTokens).build());
        }
        return options;
    }

}
