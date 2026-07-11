package com.phoenix.agent.workflow.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class CustomMessageDeserializer extends JsonDeserializer<CustomMessage> {
    @Override
    public CustomMessage deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        String content = null;
        String type = null;
        p.nextToken(); // 跳过 START_OBJECT
        while (p.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = p.getCurrentName();
            p.nextToken();
            if ("content".equals(fieldName)) {
                content = p.getText();
            } else if ("type".equals(fieldName)) {
                type = p.getText();
            }
        }
        return new CustomMessage(content, type);
    }
}
