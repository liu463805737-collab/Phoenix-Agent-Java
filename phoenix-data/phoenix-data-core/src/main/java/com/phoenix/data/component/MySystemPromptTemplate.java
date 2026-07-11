package com.phoenix.data.component;

import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MySystemPromptTemplate {

    public String getPrompt(Resource systemResource) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
        return systemPromptTemplate.render();
    }

    public String getPrompt(Resource systemResource, Map<String, Object> variables) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
        return systemPromptTemplate.render(variables);
    }
}
