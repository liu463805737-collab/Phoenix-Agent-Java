package com.phoenix.test.harness;

import cn.hutool.core.lang.UUID;
import com.phoenix.admin.PhoenixAgentApplication;
import com.phoenix.agent.harness.request.ConfirmRequest;
import com.phoenix.agent.harness.request.HarnessRequest;
import com.phoenix.agent.harness.send.HarnessSendMessage;
import com.phoenix.agent.harness.service.HitlCacheService;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ConfirmResult;
import io.agentscope.core.event.RequireUserConfirmEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.message.Msg;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;

@Slf4j
@SpringBootTest(classes = PhoenixAgentApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HarnessAgentTest {
    @Autowired
    private HarnessSendMessage harnessSendMessage;
    @Autowired
    private HitlCacheService hitlCacheService;

    @Test
    public void testHuman2() {
        /**
         * 第一次发起的时候
         */
        String sessionId = UUID.randomUUID().toString();
        String userId = "test2";
        HarnessRequest request = HarnessRequest.builder().userId(userId).sessionId(sessionId).message("给订单 123 退款 100 元").build();
        System.out.println("=== 测试2：退款（触发人工确认） ===");
        harnessSendMessage.stream("HumanInTheLoop", request)// 核心：在事件流中拦截确认事件，并注入确认结果
                .doOnNext(event -> {
                    if (event instanceof RequireUserConfirmEvent confirmEvent) {
                        hitlCacheService.savePendingConfirm(sessionId, confirmEvent);
                    } else {
                        if (event instanceof TextBlockDeltaEvent textBlockDeltaEvent) {
                            System.out.println("-----" + textBlockDeltaEvent.getDelta());
                        }
                    }
                })
                .blockLast(); // 阻塞等待整个流程结束;

        /**
         * 第二次人工确认阶段
         */
        ConfirmRequest confirmRequest = new ConfirmRequest();
        confirmRequest.setUserId(userId);
        confirmRequest.setSessionId(sessionId);
        confirmRequest.setAllowed(true);
        harnessSendMessage.confirmStream("HumanInTheLoop", confirmRequest).doOnNext(event -> {
            if (event instanceof TextBlockDeltaEvent textBlockDeltaEvent) {
                System.out.println("++++++++++++++" + textBlockDeltaEvent.getDelta());
            }
        }).blockLast();

    }


    @Test
    public void testHuman() {
        String sessionId = UUID.randomUUID().toString();
        String userId = "test2";
        HarnessRequest request = HarnessRequest.builder().userId(userId).sessionId(sessionId).message("查订单 123").build();
//        // 5. 连续测试三种干预场景
//        System.out.println("=== 测试1：查订单（直接执行） ===");
//        Flux<AgentEvent> agentEventFlux = harnessSendMessage.stream("HumanInTheLoop", request);
//        StepVerifier.create(agentEventFlux)
//                .recordWith(ArrayList::new)  // 记录所有事件
//                .thenConsumeWhile(event -> {
//                    if (event instanceof TextBlockDeltaEvent delta){
//                        log.info("返回的内容：{}",delta.getDelta());
//                    }
//                    return true;
//                })
//                .verifyComplete();
        sessionId = UUID.randomUUID().toString();
        request = HarnessRequest.builder().userId(userId).sessionId(sessionId).message("给订单 123 退款 100 元").build();
        System.out.println("=== 测试2：退款（触发人工确认） ===");
        harnessSendMessage.stream("HumanInTheLoop", request)// 核心：在事件流中拦截确认事件，并注入确认结果
                .handle((event, sink) -> {
                    if (event instanceof RequireUserConfirmEvent confirmEvent) {
                        System.out.println("后台拦截到确认请求，自动批准...");
                        // 构造确认结果（允许执行，并带上原始工具调用）
                        ConfirmResult result = new ConfirmResult(
                                true,
                                confirmEvent.getToolCalls().get(0),
                                null // 建议规则，测试时可为 null
                        );
                        // 将确认结果注入到事件流中，唤醒 Agent 继续执行
                        sink.next(result);
                    } else {
                        if (event instanceof TextBlockDeltaEvent textBlockDeltaEvent) {
                            System.out.println(textBlockDeltaEvent.getDelta());
                        }
                        sink.next(event);
                    }
                })
                // 订阅并等待执行完成
                .doOnNext(event -> System.out.println("收到后续事件: " + event.getClass().getSimpleName()))
                .blockLast(); // 阻塞等待整个流程结束;
        sessionId = UUID.randomUUID().toString();
        request = HarnessRequest.builder().userId(userId).sessionId(sessionId).message("删掉 orders 表").build();
        System.out.println("=== 测试3：删表（直接拒绝） ===");
        Flux<AgentEvent> agentEventFlux = harnessSendMessage.stream("HumanInTheLoop", request);
        StepVerifier.create(agentEventFlux)
                .recordWith(ArrayList::new)  // 记录所有事件
                .thenConsumeWhile(event -> {
                    if (event instanceof TextBlockDeltaEvent delta) {
                        log.info("返回的内容：{}", delta.getDelta());
                    }
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void testCallAgent() {
        HarnessRequest request = HarnessRequest.builder().userId("test1").sessionId(UUID.fastUUID().toString()).message("请帮我查询请假流程").build();
        Mono<Msg> msgMono = harnessSendMessage.call("RulesHarnessAgent", request);
        log.info("content:{}", msgMono.block().getTextContent());
    }

    @Test
    public void testStreamAgent() {
        HarnessRequest request = HarnessRequest.builder().userId("test1").sessionId(UUID.fastUUID().toString()).message("请帮我查询请假流程").build();
        Flux<AgentEvent> agentEventFlux = harnessSendMessage.stream("RulesHarnessAgent", request);
        StepVerifier.create(agentEventFlux)
                .recordWith(ArrayList::new)  // 记录所有事件
                .thenConsumeWhile(event -> {
                    if (event instanceof TextBlockDeltaEvent delta) {
                        log.info("返回的内容：{}", delta.getDelta());
                    }
                    return true;
                })
                .verifyComplete();  // 等待流结束
    }
}
