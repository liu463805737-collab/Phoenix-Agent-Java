package com.phoenix.data.service.llm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.openai.OpenAiChatOptions;

public abstract class AbstractLlmService {

    /**
     * 获取最大的token
     * @param system 系统提示词
     * @param user 用户提示词
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
            maxTokens =  fullPrompt.length() * 2;
        }
        if (maxTokens > 50000){
            maxTokens = 30000;
        }
        return maxTokens;
    }

    protected  OpenAiChatOptions setMaxtokens(String system, String user) {
        Integer maxTokens = maxToken(system, user);
        return OpenAiChatOptions.builder().maxTokens(maxTokens).build();
    }
}
