package com.phoenix.agent.harness.send.impl;

import com.phoenix.agent.harness.request.HarnessRequest;
import com.phoenix.agent.harness.send.HarnessSendMessage;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.message.UserMessage;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class HarnessSendMessageImpl implements HarnessSendMessage {
    @Override
    public String call(HarnessAgent agent, HarnessRequest request) {
        return agent.call(buildUserMessage(request), buildRuntimeContext(request)).block().getTextContent();
    }

    @Override
    public Flux<AgentEvent> stream(HarnessAgent agent, HarnessRequest request) {
        return agent.streamEvents(buildUserMessage(request), buildRuntimeContext(request));
    }

    private RuntimeContext buildRuntimeContext(HarnessRequest request) {
        return RuntimeContext.builder().sessionId(request.getSessionId()).userId(request.getUserId()).build();
    }

    private UserMessage buildUserMessage(HarnessRequest request) {
        return new UserMessage(request.getMessage());
    }
}
