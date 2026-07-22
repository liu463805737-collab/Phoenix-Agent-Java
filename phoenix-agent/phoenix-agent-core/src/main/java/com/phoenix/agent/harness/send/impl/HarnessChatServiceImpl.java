package com.phoenix.agent.harness.send.impl;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.phoenix.agent.harness.agent.HarnessStaticLoader;
import com.phoenix.agent.harness.request.ConfirmRequest;
import com.phoenix.agent.harness.request.HarnessRequest;
import com.phoenix.agent.harness.send.HarnessChatService;
import com.phoenix.agent.harness.service.HitlCacheService;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.AgentEventType;
import io.agentscope.core.event.ConfirmResult;
import io.agentscope.core.event.CustomEvent;
import io.agentscope.core.event.RequireUserConfirmEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class HarnessChatServiceImpl implements HarnessChatService {
    private final HarnessStaticLoader harnessStaticLoader;
    private final HitlCacheService hitlCacheService;
    @Override
    public Mono<Msg> call(String sn, HarnessRequest request) {
        HarnessAgent harnessAgent = getHarnessAgent(sn);
        return harnessAgent.call(buildUserMessage(request), buildRuntimeContext(request));
    }

    @Override
    public Flux<NodeOutput> stream(String sn, HarnessRequest request) {
        HarnessAgent harnessAgent = getHarnessAgent(sn);
        String sessionId = request.getSessionId();
        return harnessAgent.streamEvents(buildUserMessage(request), buildRuntimeContext(request))
                .map(event -> toNodeOutput(event, sessionId));
    }

    @Override
    public HarnessAgent getHarnessAgent(String sn) {
        return harnessStaticLoader.loadAgent(sn);
    }

    @Override
    public Flux<NodeOutput> confirmStream(String sn, ConfirmRequest request) {
        RequireUserConfirmEvent pendingConfirm = hitlCacheService.getAndRemovePendingConfirm(request.getSessionId());
        if (pendingConfirm == null) {
            Map<String, Object> context = new HashMap<>();
            context.put("confirm", "确认已过期或未找到任务");
            CustomEvent agentEvent = new CustomEvent("confirm", context);
            Map<String, Object> data = new HashMap<>();
            data.put("agent_event", agentEvent);
            return Flux.just(NodeOutput.of("harness_agent", "harness", new OverAllState(data), null));
        }
        ConfirmResult result = new ConfirmResult(
                request.isAllowed(),
                pendingConfirm.getToolCalls().get(0),
                request.isAllowed() ? request.getSuggestedRules() : null
        );
        UserMessage confirmMsg = UserMessage.builder()
                .name("system")
                .textContent("User confirmed, continue.")
                .metadata(Map.of(Msg.METADATA_CONFIRM_RESULTS, List.of(result)))
                .build();
        String sessionId = request.getSessionId();
        return getHarnessAgent(sn).streamEvents(confirmMsg, RuntimeContext.builder().userId(request.getUserId()).sessionId(sessionId).build())
                .map(event -> toNodeOutput(event, sessionId));
    }

    private NodeOutput toNodeOutput(AgentEvent event, String sessionId) {
        if (event.getType() == AgentEventType.TEXT_BLOCK_DELTA && event instanceof TextBlockDeltaEvent textEvent) {
            return new StreamingOutput<>(textEvent.getDelta(), "harness_agent", "harness", new OverAllState());
        }
        if (event.getType() == AgentEventType.AGENT_END) {
            return NodeOutput.of(StateGraph.END, "harness", new OverAllState(), null);
        }
        if (event instanceof RequireUserConfirmEvent confirmEvent) {
            hitlCacheService.savePendingConfirm(sessionId, confirmEvent);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("agent_event", event);
        return NodeOutput.of("harness_agent", "harness", new OverAllState(data), null);
    }

    private RuntimeContext buildRuntimeContext(HarnessRequest request) {
        return RuntimeContext.builder().sessionId(request.getSessionId()).userId(request.getUserId()).build();
    }

    private UserMessage buildUserMessage(HarnessRequest request) {
        return new UserMessage(request.getMessage());
    }
}
