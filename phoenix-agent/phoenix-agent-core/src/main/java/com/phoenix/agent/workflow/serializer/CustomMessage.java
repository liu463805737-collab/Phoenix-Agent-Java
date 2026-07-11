package com.phoenix.agent.workflow.serializer;

import java.io.Serializable;

public class CustomMessage implements Serializable {
    private String content;
    private String type;

    // 构造函数、Getter、Setter...
    public CustomMessage(String content, String type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() { return content; }
    public String getType() { return type; }
}
