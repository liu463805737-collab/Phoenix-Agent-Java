package com.phoenix.test.harness;

import cn.hutool.core.lang.UUID;
import com.phoenix.admin.PhoenixAgentApplication;
import com.phoenix.agent.harness.agent.rules.RulesReactAgent;
import com.phoenix.agent.harness.request.HarnessRequest;
import com.phoenix.agent.harness.send.HarnessSendMessage;
import io.agentscope.core.event.AgentEvent;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;

@Slf4j
@SpringBootTest(classes = PhoenixAgentApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HarnessAgentTest {
    @Autowired
    private RulesReactAgent rulesReactAgent;
    @Autowired
    private HarnessSendMessage harnessSendMessage;

    @Test
    public void testCallAgent() {
        HarnessRequest request = HarnessRequest.builder().userId("test1").sessionId(UUID.fastUUID().toString()).message("请帮我查询请假流程").build();
        String content = harnessSendMessage.call(rulesReactAgent.createReActAgent(), request);
        log.info("content:{}", content);
    }

    @Test
    public void testStreamAgent() {
        HarnessRequest request = HarnessRequest.builder().userId("test1").sessionId(UUID.fastUUID().toString()).message("请帮我查询请假流程").build();
        Flux<AgentEvent> agentEventFlux = harnessSendMessage.stream(rulesReactAgent.createReActAgent(), request);
        StepVerifier.create(agentEventFlux)
                .recordWith(ArrayList::new)  // 记录所有事件
                .thenConsumeWhile(event -> {
                    log.info("收到事件: {}", event);
                    return true;
                })
                .verifyComplete();  // 等待流结束
    }
}
