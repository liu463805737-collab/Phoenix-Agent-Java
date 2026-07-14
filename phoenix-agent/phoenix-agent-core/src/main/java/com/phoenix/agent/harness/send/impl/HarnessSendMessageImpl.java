package com.phoenix.agent.harness.send.impl;

import com.phoenix.agent.harness.agent.HarnessStaticLoader;
import com.phoenix.agent.harness.request.HarnessRequest;
import com.phoenix.agent.harness.send.HarnessSendMessage;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class HarnessSendMessageImpl implements HarnessSendMessage {
    @Autowired
    private HarnessStaticLoader harnessStaticLoader;
    @Override
    public Msg call(String sn, HarnessRequest request) {
        HarnessAgent harnessAgent = harnessStaticLoader.loadAgent(sn);
        return harnessAgent.call(buildUserMessage(request), buildRuntimeContext(request)).block();
    }

    @Override
    public Flux<AgentEvent> stream(String sn, HarnessRequest request) {
        HarnessAgent harnessAgent = harnessStaticLoader.loadAgent(sn);
        return harnessAgent.streamEvents(buildUserMessage(request), buildRuntimeContext(request));
    }

    private RuntimeContext buildRuntimeContext(HarnessRequest request) {
        return RuntimeContext.builder().sessionId(request.getSessionId()).userId(request.getUserId()).build();
    }

    private UserMessage buildUserMessage(HarnessRequest request) {
        return new UserMessage(request.getMessage());
    }
}
