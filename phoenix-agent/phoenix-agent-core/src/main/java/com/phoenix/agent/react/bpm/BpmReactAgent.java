package com.phoenix.agent.react.bpm;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.phoenix.agent.react.AbstractReactAgent;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component("bpmReactAgent")
public class BpmReactAgent extends AbstractReactAgent {
    public static final String SN = "BpmReactAgent";
    public static final String NAME = "流程分析小助手";
    @Value("classpath:/system/bpm/customer_bpm_system.st")
    private Resource systemResource;
    @Autowired
    private BpmToolSearch bpmToolSearch;
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
                .methodTools(bpmToolSearch)
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
        return "分析流程的使用情况";
    }

}
