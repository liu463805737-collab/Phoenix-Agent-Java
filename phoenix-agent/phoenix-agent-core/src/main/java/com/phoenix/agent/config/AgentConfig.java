package com.phoenix.agent.config;

import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.summarization.SummarizationHook;
import com.phoenix.agent.service.store.CombinedStoreService;
import com.phoenix.agent.service.vectorstore.PostgresqlCombinedStore;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {
    @Bean
    public PostgresqlCombinedStore pgStore(CombinedStoreService storeService) {
        return new PostgresqlCombinedStore(storeService);
    }
    /**
     * 消息压缩
     * @return
     */
    @Bean
    public SummarizationHook summarizationHook(@Qualifier("openAiChatModel") ChatModel chatModel) {
        return SummarizationHook.builder()
                .model(chatModel)
                .maxTokensBeforeSummary(8000)
                .messagesToKeep(20)
                .build();
    }

    /**
     * 创建最多调用五次的钩子
     *
     * @return
     */
    @Bean
    public ModelCallLimitHook modelCallLimitHook() {
        return ModelCallLimitHook.builder().runLimit(8)  // 限制最多调用 8 次
                .exitBehavior(ModelCallLimitHook.ExitBehavior.ERROR)  // 超出限制时抛出异常
                .build();
    }
}
