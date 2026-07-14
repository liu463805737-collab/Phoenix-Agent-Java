package com.phoenix.test.harness;

import cn.hutool.core.lang.UUID;
import com.phoenix.admin.PhoenixAgentApplication;
import com.phoenix.agent.harness.request.HarnessRequest;
import com.phoenix.agent.harness.send.HarnessSendMessage;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.message.Msg;
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
    private HarnessSendMessage harnessSendMessage;

    @Test
    public void testCallAgent() {
        HarnessRequest request = HarnessRequest.builder().userId("test1").sessionId(UUID.fastUUID().toString()).message("请帮我查询请假流程").build();
        Msg msg = harnessSendMessage.call("RulesHarnessAgent", request);
        log.info("content:{}", msg.getTextContent());
    }

    @Test
    public void testStreamAgent() {
        HarnessRequest request = HarnessRequest.builder().userId("test1").sessionId(UUID.fastUUID().toString()).message("请帮我查询请假流程").build();
        Flux<AgentEvent> agentEventFlux = harnessSendMessage.stream("RulesHarnessAgent", request);
        StepVerifier.create(agentEventFlux)
                .recordWith(ArrayList::new)  // 记录所有事件
                .thenConsumeWhile(event -> {
                    if (event instanceof TextBlockDeltaEvent delta){
                        log.info("返回的内容：{}",delta.getDelta());
                    }
                    return true;
                })
                .verifyComplete();  // 等待流结束
    }
}
