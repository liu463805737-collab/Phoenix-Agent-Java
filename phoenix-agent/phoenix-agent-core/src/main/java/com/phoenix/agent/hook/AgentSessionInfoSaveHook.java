package com.phoenix.agent.hook;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.JumpTo;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentSessionInfoSaveHook extends ModelHook {

    private final TransactionTemplate transactionTemplate;

    @Override
    public String getName() {
        return "AgentSessionInfoSaveHook";
    }

    @Override
    public List<ModelInterceptor> getModelInterceptors() {
        return super.getModelInterceptors();
    }

    @Override
    public List<ToolInterceptor> getToolInterceptors() {
        return super.getToolInterceptors();
    }

    @Override
    public List<ToolCallback> getTools() {
        return super.getTools();
    }

    @Override
    public List<JumpTo> canJumpTo() {
        return super.canJumpTo();
    }

    @Override
    public Map<String, KeyStrategy> getKeyStrategys() {
        return super.getKeyStrategys();
    }

    @Override
    public HookPosition[] getHookPositions() {
        return super.getHookPositions();
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterModel(OverAllState state, RunnableConfig config) {
        String input = "";
        Optional<Object> inputObj = state.value("input");
        if (inputObj.isPresent()) {
            input = inputObj.get().toString();
            if (input.length() > 255) {
                input = input.substring(0, 200);
            }
        }
        String sessionId;
        Optional<Object> sessionIdObj = config.metadata("sessionId");
        if (sessionIdObj.isPresent()) {
            sessionId = sessionIdObj.get().toString();
        } else {
            sessionId = "";
        }
        String agentSn = "";
        Optional<Object> agentSnObj = config.metadata("agentSn");
        if (sessionIdObj.isPresent()) {
            agentSn = agentSnObj.get().toString();
        }
        String text = "";
        String sessionTitle = "";
        Map<String, Object> data = state.data();
        if (CollUtil.isNotEmpty(data)) {
            Optional<Object> messages = state.value("messages");
            if (messages.isPresent()) {
                Object o = messages.get();
                if (o instanceof ArrayList) {
                    List<AssistantMessage> assistantMessages = new ArrayList<>();
                    List<UserMessage> userMessages = new ArrayList<>();
                    ArrayList<Object> mgs = (ArrayList<Object>) o;
                    if (CollUtil.isNotEmpty(mgs)) {
                        for (Object mg : mgs) {
                            if (mg instanceof AssistantMessage) {
                                assistantMessages.add((AssistantMessage) mg);
                            }
                            if (mg instanceof UserMessage) {
                                userMessages.add((UserMessage) mg);
                            }
                        }
                    }
                    if (CollUtil.isNotEmpty(assistantMessages)) {
                        AssistantMessage assistantMessage = assistantMessages.get(assistantMessages.size() - 1);
                        text = assistantMessage.getText();
                    }
                    if (CollUtil.isNotEmpty(userMessages)) {
                        UserMessage userMessage = userMessages.get(0);
                        sessionTitle = userMessage.getText();
                    } else {
                        sessionTitle = input;
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(Map.of());
    }
}
