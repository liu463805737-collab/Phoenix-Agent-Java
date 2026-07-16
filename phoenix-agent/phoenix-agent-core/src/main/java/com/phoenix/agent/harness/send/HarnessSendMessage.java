package com.phoenix.agent.harness.send;

import com.phoenix.agent.harness.request.ConfirmRequest;
import com.phoenix.agent.harness.request.HarnessRequest;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface HarnessSendMessage {
    /**
     * 发送消息堵塞式返回
     * @param sn sn
     * @param request request
     * @return
     */
    Mono<Msg> call(String sn, HarnessRequest request);

    /**
     * 发送消息流式消息返回
     * @param sn sn
     * @param request request
     * @return
     */
    Flux<AgentEvent> stream(String sn, HarnessRequest request);

    /**
     * 获取智能体
     * @param sn 标识
     * @return
     */
    HarnessAgent getHarnessAgent(String sn);

    /**
     * 人工确认
     * @param request 参数
     * @return
     */
    Flux<? extends Object> confirm(String sn, ConfirmRequest request);
}
