package com.phoenix.agent.workflow.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class CustomMessageSerializer extends JsonSerializer<CustomMessage> {
    @Override
    public void serialize(CustomMessage value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartObject();
        gen.writeStringField("content", value.getContent());
        gen.writeStringField("type", value.getType());
        gen.writeEndObject();
    }
}
