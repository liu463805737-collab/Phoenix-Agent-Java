package com.phoenix.test.harness;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.thread.ThreadUtil;
import com.phoenix.admin.PhoenixAgentApplication;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.phoenix.agent.harness.request.ConfirmRequest;
import com.phoenix.agent.harness.request.HarnessRequest;
import com.phoenix.agent.harness.send.HarnessChatService;
import com.phoenix.agent.harness.service.HitlCacheService;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ConfirmResult;
import io.agentscope.core.event.RequireUserConfirmEvent;
import io.agentscope.core.message.Msg;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@SpringBootTest(classes = PhoenixAgentApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HarnessAgentTest {
    @Autowired
    private HarnessChatService harnessChatService;
    @Autowired
    private HitlCacheService hitlCacheService;

    @Test
    public void testHuman2() {
        /**
         * 第一次发起的时候
         */
        String sessionId = UUID.randomUUID().toString();
        String userId = "test2";
        HarnessRequest request = HarnessRequest.builder().userId(userId).sessionId(sessionId).message("给订单号 123 退款 100 元").build();
        System.out.println("=== 测试2：退款（触发人工确认） ===");
        harnessChatService.stream("HumanInTheLoop", request)
                .doOnNext(output -> {
                    if (output instanceof StreamingOutput<?> streamingOutput && streamingOutput.chunk() != null) {
                        System.out.println("-----" + streamingOutput.chunk());
                    }
                    output.state().value("agent_event", AgentEvent.class).ifPresent(event -> {
                        if (event instanceof RequireUserConfirmEvent confirmEvent) {
                            hitlCacheService.savePendingConfirm(sessionId, confirmEvent);
                        }
                    });
                })
                .blockLast();

        /**
         * 第二次人工确认阶段
         */
        ConfirmRequest confirmRequest = new ConfirmRequest();
        confirmRequest.setUserId(userId);
        confirmRequest.setSessionId(sessionId);
        confirmRequest.setAllowed(true);
        harnessChatService.confirmStream("HumanInTheLoop", confirmRequest).doOnNext(output -> {
            if (output instanceof StreamingOutput<?> streamingOutput && streamingOutput.chunk() != null) {
                System.out.println("++++++++++++++" + streamingOutput.chunk());
            }
        }).subscribe();

        ThreadUtil.safeSleep(100000);

    }


    @Test
    public void testHuman() {
        String sessionId = UUID.randomUUID().toString();
        String userId = "test2";
        HarnessRequest request = HarnessRequest.builder().userId(userId).sessionId(sessionId).message("查订单 123").build();
//        // 5. 连续测试三种干预场景
//        System.out.println("=== 测试1：查订单（直接执行） ===");
//        Flux<NodeOutput> agentEventFlux = harnessChatService.stream("HumanInTheLoop", request);
//        StepVerifier.create(agentEventFlux)
//                .recordWith(ArrayList::new)
//                .thenConsumeWhile(output -> {
//                    if (output instanceof StreamingOutput<?> streamingOutput && streamingOutput.chunk() != null){
//                        log.info("返回的内容：{}", streamingOutput.chunk());
//                    }
//                    return true;
//                })
//                .verifyComplete();
        sessionId = UUID.randomUUID().toString();
        request = HarnessRequest.builder().userId(userId).sessionId(sessionId).message("给订单 123 退款 100 元").build();
        System.out.println("=== 测试2：退款（触发人工确认） ===");
        harnessChatService.stream("HumanInTheLoop", request)
                .handle((output, sink) -> {
                    AgentEvent event = output.state().value("agent_event", AgentEvent.class).orElse(null);
                    if (event instanceof RequireUserConfirmEvent confirmEvent) {
                        System.out.println("后台拦截到确认请求，自动批准...");
                        ConfirmResult result = new ConfirmResult(
                                true,
                                confirmEvent.getToolCalls().get(0),
                                null
                        );
                        sink.next(result);
                    } else {
                        if (output instanceof StreamingOutput<?> streamingOutput && streamingOutput.chunk() != null) {
                            System.out.println(streamingOutput.chunk());
                        }
                        sink.next(output);
                    }
                })
                .doOnNext(output -> System.out.println("收到后续事件: " + output.getClass().getSimpleName()))
                .blockLast();
        sessionId = UUID.randomUUID().toString();
        request = HarnessRequest.builder().userId(userId).sessionId(sessionId).message("删掉 orders 表").build();
        System.out.println("=== 测试3：删表（直接拒绝） ===");
        Flux<NodeOutput> nodeOutputFlux = harnessChatService.stream("HumanInTheLoop", request);
        StepVerifier.create(nodeOutputFlux)
                .recordWith(ArrayList::new)
                .thenConsumeWhile(output -> {
                    if (output instanceof StreamingOutput<?> streamingOutput && streamingOutput.chunk() != null) {
                        log.info("返回的内容：{}", streamingOutput.chunk());
                    }
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void testCallAgent() {
        HarnessRequest request = HarnessRequest.builder().userId("test1").sessionId(UUID.fastUUID().toString()).message("请帮我查询请假流程").build();
        Mono<Msg> msgMono = harnessChatService.call("RulesHarnessAgent", request);
        log.info("content:{}", msgMono.block().getTextContent());
    }

    @Test
    public void testStreamAgent() {
        HarnessRequest request = HarnessRequest.builder().userId("test1").sessionId(UUID.fastUUID().toString()).message("请帮我查询请假流程").build();
        Flux<NodeOutput> nodeOutputFlux = harnessChatService.stream("RulesHarnessAgent", request);
        StepVerifier.create(nodeOutputFlux)
                .recordWith(ArrayList::new)
                .thenConsumeWhile(output -> {
                    if (output instanceof StreamingOutput<?> streamingOutput && streamingOutput.chunk() != null) {
                        log.info("返回的内容：{}", streamingOutput.chunk());
                    }
                    return true;
                })
                .verifyComplete();
    }
}
