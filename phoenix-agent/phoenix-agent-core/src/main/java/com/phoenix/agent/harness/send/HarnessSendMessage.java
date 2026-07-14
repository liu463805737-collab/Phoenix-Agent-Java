package com.phoenix.agent.harness.send;

import com.phoenix.agent.harness.request.HarnessRequest;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.message.Msg;
import reactor.core.publisher.Flux;

public interface HarnessSendMessage {
    /**
     * 发送消息堵塞式返回
     * @param sn sn
     * @param request request
     * @return
     */
    Msg call(String sn, HarnessRequest request);

    /**
     * 发送消息流式消息返回
     * @param sn sn
     * @param request request
     * @return
     */
    Flux<AgentEvent> stream(String sn, HarnessRequest request);
}
