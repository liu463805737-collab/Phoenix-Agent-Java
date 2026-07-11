package com.phoenix.test;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.phoenix.admin.PhoenixAgentApplication;
import com.phoenix.agent.react.ReactAgentComponent;
import com.phoenix.agent.react.zhidu.ZhiduReactAgent;
import com.phoenix.agent.workflow.WorkflowComponent;
import com.phoenix.common.vo.login.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;


@Slf4j
@SpringBootTest(classes = PhoenixAgentApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReactAgentTest {

    @Autowired
    private ReactAgentComponent reactAgentComponent;
    @Autowired
    private WorkflowComponent workflowComponent;

    @Test
    public void testReactAgent() throws Exception {
        String userMsg = "请假流程";
        /**
         * 1、从header里面获取sessionId
         * 2、如果不存在创建
         * 3、如果存在判断和当前用户id是否对应 不对应重新创建sessionId并放入header里面
         */
        var userId = "user_10086";
        String sessionId = UUID.randomUUID().toString();
        var userProfile = UserProfile.builder().sessionId(sessionId).userId(userId).userCode("10086").email("test@example.com").name("军哥").occupation("自由职业者").build();
//        String text = reactAgentComponent.call(BusinessReactAgent.NAME, userMsg, userProfile);
//        sessionManager.refreshSession(sessionId);
//        log.info("分析结果如下：{}", text);
        NodeOutput nodeOutput = reactAgentComponent.stream(ZhiduReactAgent.SN, userMsg, userProfile).doOnNext(output -> {
            if (output instanceof StreamingOutput<?> streamingOutput) {
                if (streamingOutput != null && streamingOutput.message() != null) {
                    System.out.println("Output from node " + streamingOutput.node() + ": " + streamingOutput.message().getText());
                }
            }
        }).blockLast();
        System.out.println("\n\n最终结果，包含所有节点状态：\n" +nodeOutput.state().data());
//        stream.subscribe(
//                output -> {
//                    if (output instanceof StreamingOutput streaming) {
//                        // 获取消息对象
//                        Message message = streaming.message();
//
//                        // 如果是 AssistantMessage，获取文本内容
//                        if (message instanceof AssistantMessage assistantMessage) {
//                            String text = assistantMessage.getText();
//                            if (text != null && !text.isEmpty()) {
//                                System.out.print(text);
//                            }
//                        }
//                    }
//                },
//                error -> System.err.println("错误: " + error.getMessage()),
//                () -> System.out.println("\n完成")
//        );

        /**
         * 返回前端的写法
         * @GetMapping(value = "/agent/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
         * public Flux<String> streamAgent(@RequestParam String userMsg) {
         *     RunnableConfig config = RunnableConfig.builder().threadId("user_10086").build();
         *
         *     return agent.stream(userMsg, config)
         *             .filter(output -> output instanceof StreamingOutput<?>) // 过滤非流式节点
         *             .map(output -> ((StreamingOutput<?>) output).message().getText()) // 提取增量文本
         *             .filter(text -> text != null && !text.isEmpty()); // 过滤空文本
         * }
         */
    }
}
