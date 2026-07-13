package com.phoenix.test.harness;

import com.phoenix.admin.PhoenixAgentApplication;
import com.phoenix.agent.harness.agent.RulesReactAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = PhoenixAgentApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HarnessAgentTest {
    @Autowired
    private RulesReactAgent rulesReactAgent;

    @Test
    public void testAgent() {
        UserMessage userMsg = new UserMessage("请帮我查询请假流程");
        RuntimeContext runtimeContext = RuntimeContext.builder().sessionId("test1").userId("test1").build();
        String reply = rulesReactAgent.createReActAgent().call(userMsg, runtimeContext).block().getTextContent();
        System.out.println("Agent> " + reply);
    }
}
