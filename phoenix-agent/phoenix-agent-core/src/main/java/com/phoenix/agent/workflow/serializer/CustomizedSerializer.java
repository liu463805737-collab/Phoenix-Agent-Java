package com.phoenix.agent.workflow.serializer;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.serializer.plain_text.jackson.SpringAIJacksonStateSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class CustomizedSerializer extends SpringAIJacksonStateSerializer {

    public CustomizedSerializer() {
        super(OverAllState::new);
        // 创建 SimpleModule 并注册自定义序列化器/反序列化器
        var module = new SimpleModule();
        module.addSerializer(CustomMessage.class, new CustomMessageSerializer());
        module.addDeserializer(CustomMessage.class, new CustomMessageDeserializer());
        // 注册模块到 ObjectMapper
        objectMapper.registerModule(module);
        // 可以继续定制 ObjectMapper，例如：
        // objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }
}
