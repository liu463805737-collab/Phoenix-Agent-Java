package com.phoenix.agent.react.request;

import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.agent.interceptor.Interceptor;
import lombok.Data;

import java.util.List;
@Data
public class ReactAgentRequest {
    private Long agentId;
    private List<Object> tools;
    private String userPrompt;
    private String instruction;
    private List<Hook> hooks;
    private List<Interceptor> interceptors;
}
