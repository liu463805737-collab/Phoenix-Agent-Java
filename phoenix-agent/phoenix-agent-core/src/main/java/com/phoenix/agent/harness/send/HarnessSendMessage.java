package com.phoenix.agent.harness.send;

import com.phoenix.agent.harness.request.HarnessRequest;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.harness.agent.HarnessAgent;
import reactor.core.publisher.Flux;

public interface HarnessSendMessage {
    /**
     * 发送消息堵塞式返回
     * @param agent agent
     * @param request request
     * @return
     */
    Msg call(HarnessAgent agent, HarnessRequest request);

    /**
     * 发送消息流式消息返回
     * @param agent agent
     * @param request request
     * @return
     */
    Flux<AgentEvent> stream(HarnessAgent agent, HarnessRequest request);
}
