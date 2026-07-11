package com.phoenix.agent.react.zhidu;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.phoenix.agent.react.AbstractReactAgent;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component("zhiduReactAgent")
public class ZhiduReactAgent extends AbstractReactAgent {
    public static final String SN = "ZhiduReactAgent";
    public static final String NAME = "制度分析小助手";
    @Value("classpath:/system/rules/customer_rules_system.st")
    private Resource systemResource;
    @Autowired
    private ZhiduToolSearch zhiduToolSearch;
    /**
     * 获取ReactAgent
     *
     * @return ReactAgent
     */
    public ReactAgent createReactAgent() {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
        String systemPrompt = systemPromptTemplate.render();
        ReactAgent reactAgent = ReactAgent.builder()
                .name(SN)
                .model(this.createChatModel())
                .systemPrompt(systemPrompt)
                .methodTools(zhiduToolSearch)
                .interceptors(this.getCommonInterceptors())
                .hooks(this.getCommonHooks())
                .saver(saver)//短期记忆
                .build();
        return reactAgent;
    }

    @Override
    public String getSn() {
        return SN;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "制度汇编";
    }

}
