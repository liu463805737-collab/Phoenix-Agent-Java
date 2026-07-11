package com.phoenix.agent.dto;

import lombok.Data;

@Data
public class ChatModelRequest {
    /**
     * 当前会话的sessionId
     */
    private String sessionId;
    /**
     * 用户输入的信息
     */
    private String content;
    /**
     * 智能体标示
     */
    private String agentSn;
}
