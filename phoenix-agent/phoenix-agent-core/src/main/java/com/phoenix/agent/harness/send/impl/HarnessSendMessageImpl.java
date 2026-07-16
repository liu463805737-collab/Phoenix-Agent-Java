package com.phoenix.agent.harness.send.impl;

import com.phoenix.agent.harness.agent.HarnessStaticLoader;
import com.phoenix.agent.harness.request.ConfirmRequest;
import com.phoenix.agent.harness.request.HarnessRequest;
import com.phoenix.agent.harness.send.HarnessSendMessage;
import com.phoenix.agent.harness.service.HitlCacheService;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ConfirmResult;
import io.agentscope.core.event.CustomEvent;
import io.agentscope.core.event.RequireUserConfirmEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class HarnessSendMessageImpl implements HarnessSendMessage {
    private final HarnessStaticLoader harnessStaticLoader;
    private final HitlCacheService hitlCacheService;
    @Override
    public Mono<Msg> call(String sn, HarnessRequest request) {
        HarnessAgent harnessAgent = harnessStaticLoader.loadAgent(sn);
        return harnessAgent.call(buildUserMessage(request), buildRuntimeContext(request));
    }

    @Override
    public Flux<AgentEvent> stream(String sn, HarnessRequest request) {
        HarnessAgent harnessAgent = harnessStaticLoader.loadAgent(sn);
        return harnessAgent.streamEvents(buildUserMessage(request), buildRuntimeContext(request));
    }

    @Override
    public HarnessAgent getHarnessAgent(String sn) {
        return harnessStaticLoader.loadAgent(sn);
    }

    @Override
    public Flux<AgentEvent> confirmStream(String sn, ConfirmRequest request) {
        // 1. 从 Redis 中获取挂起的上下文
        RequireUserConfirmEvent pendingConfirm = hitlCacheService.getAndRemovePendingConfirm(request.getSessionId());
        if (pendingConfirm == null) {
            Map<String, Object> context = new HashMap<>();
            context.put("confirm", "确认已过期或未找到任务");
            AgentEvent agentEvent = new CustomEvent("confirm",context);
            return Flux.just(agentEvent);
        }
        // 2. 构造确认结果
        ConfirmResult result = new ConfirmResult(
                request.isAllowed(),
                pendingConfirm.getToolCalls().get(0),
                request.isAllowed() ? request.getSuggestedRules() : null
        );
        // 3. 唤醒 Agent（将结果通过 metadata 传入，ReActAgent 从 Msg.METADATA_CONFIRM_RESULTS 提取）
        UserMessage confirmMsg = UserMessage.builder()
                .name("system")
                .textContent("User confirmed, continue.")
                .metadata(Map.of(Msg.METADATA_CONFIRM_RESULTS, List.of(result)))
                .build();
        return getHarnessAgent(sn).streamEvents(confirmMsg, RuntimeContext.builder().userId(request.getUserId()).sessionId(request.getSessionId()).build());
    }

    private RuntimeContext buildRuntimeContext(HarnessRequest request) {
        return RuntimeContext.builder().sessionId(request.getSessionId()).userId(request.getUserId()).build();
    }

    private UserMessage buildUserMessage(HarnessRequest request) {
        return new UserMessage(request.getMessage());
    }
}
