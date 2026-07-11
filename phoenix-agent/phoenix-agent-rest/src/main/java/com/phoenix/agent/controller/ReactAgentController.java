package com.phoenix.agent.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenix.agent.AgentManager;
import com.phoenix.agent.dto.ChatModelRequest;
import com.phoenix.agent.vo.AgentInfoDto;
import com.phoenix.common.vo.login.UserProfile;
import com.phoenix.data.dto.GraphRequest;
import com.phoenix.data.service.graph.GraphService;
import com.phoenix.data.vo.GraphNodeResponse;
import com.phoenix.privilege.entity.PrivilegeUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.phoenix.data.constant.Constant.STREAM_EVENT_COMPLETE;
import static com.phoenix.data.constant.Constant.STREAM_EVENT_ERROR;
import static com.phoenix.privilege.constant.CommonConstant.LOGIN_USER_INFO;

/**
 * @author burce.liu
 */
@Slf4j
@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/admin/agent")
public class ReactAgentController {
    private final AgentManager agentManager;
    private final GraphService graphService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Object>> chat(@RequestBody ChatModelRequest request) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            PrivilegeUser privilegeUser = getCurrentUser();
            if (privilegeUser == null) {
                return Flux.error(new RuntimeException("用户未登录"));
            }
            String sessionId = request.getSessionId();
            var userProfile = UserProfile.builder().sessionId(sessionId).userCode(privilegeUser.getCode()).userId(userId).email(privilegeUser.getEmail()).email(privilegeUser.getEmail()).name(privilegeUser.getRealName()).build();
            AgentInfoDto agentInfoDto = AgentInfoDto.builder().sn(request.getAgentSn()).userProfile(userProfile).message(request.getContent()).build();
            return agentManager.streamCall(agentInfoDto).map(output -> {
                        Map<String, Object> event = new LinkedHashMap<>();
                        event.put("content", "");
                        event.put("end", false);
                        if (output instanceof StreamingOutput<?> streamingOutput && streamingOutput.chunk() != null && streamingOutput.getOutputType() != null && streamingOutput.getOutputType().name().endsWith("_STREAMING") && !output.isEND()) {
                            event.put("content", streamingOutput.chunk());
                        }
                        if (output.isEND()) {
                            event.put("end", true);
                        }
                        return event;
                    });
        } catch (Exception e) {
            return Flux.error(e);
        }
    }

    /**
     * 流式搜索接口，返回SSE事件流
     *
     * @param agentId              智能体ID
     * @param threadId             线程ID（可选）
     * @param query                查询内容
     * @param humanFeedback        是否需要人工反馈
     * @param humanFeedbackContent 人工反馈内容
     * @param rejectedPlan         是否拒绝计划
     * @param nl2sqlOnly           是否仅NL2SQL模式
     * @param response             HTTP响应
     * @return SSE事件流
     */
    @GetMapping(value = "/stream/chatsql", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<GraphNodeResponse>> streamSearch(@RequestParam("agentId") String agentId,
                                                                 @RequestParam(value = "threadId", required = false) String threadId, @RequestParam("query") String query,
                                                                 @RequestParam(value = "humanFeedback", required = false) boolean humanFeedback,
                                                                 @RequestParam(value = "humanFeedbackContent", required = false) String humanFeedbackContent,
                                                                 @RequestParam(value = "rejectedPlan", required = false) boolean rejectedPlan,
                                                                 @RequestParam(value = "nl2sqlOnly", required = false) boolean nl2sqlOnly, ServerHttpResponse response) {
        // Set SSE-related HTTP headers
        response.getHeaders().add("Cache-Control", "no-cache");
        response.getHeaders().add("Connection", "keep-alive");
        response.getHeaders().add("Access-Control-Allow-Origin", "*");

        Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink = Sinks.many().unicast().onBackpressureBuffer();

        GraphRequest request = GraphRequest.builder()
                .agentId(agentId)
                .threadId(threadId)
                .query(query)
                .humanFeedback(humanFeedback)
                .humanFeedbackContent(humanFeedbackContent)
                .rejectedPlan(rejectedPlan)
                .nl2sqlOnly(nl2sqlOnly)
                .build();
        graphService.graphStreamProcess(sink, request);

        return sink.asFlux().filter(sse -> {
                    // 1. 如果 event 是 "complete" 或 "error"，直接放行（不管 text 是否为空）
                    if (STREAM_EVENT_COMPLETE.equals(sse.event()) || STREAM_EVENT_ERROR.equals(sse.event())) {
                        return true;
                    }
                    // 判断字符串是否为空
                    return sse.data() != null && sse.data().getText() != null && !sse.data().getText().isEmpty();
                })
                .doOnSubscribe(subscription -> log.info("Client subscribed to stream, threadId: {}", request.getThreadId()))
                .doOnCancel(() -> {
                    log.info("Client disconnected from stream, threadId: {}", request.getThreadId());
                    if (request.getThreadId() != null) {
                        graphService.stopStreamProcessing(request.getThreadId());
                    }
                })
                .doOnError(e -> {
                    log.error("Error occurred during streaming, threadId: {}: ", request.getThreadId(), e);
                    if (request.getThreadId() != null) {
                        graphService.stopStreamProcessing(request.getThreadId());
                    }
                })
                .doOnComplete(() -> log.info("Stream completed successfully, threadId: {}", request.getThreadId()));
    }

    private PrivilegeUser getCurrentUser() {
        Object value = StpUtil.getSession().get(LOGIN_USER_INFO);
        if (value instanceof PrivilegeUser pUser) {
            return pUser;
        }
        ObjectMapper mapper = new ObjectMapper();
        if (value instanceof Map<?, ?> map) {
            return mapper.convertValue(map, PrivilegeUser.class);
        }
        if (value instanceof String str) {
            try {
                return mapper.readValue(str, PrivilegeUser.class);
            } catch (Exception e) {
                log.warn("Failed to parse PrivilegeUser from session string", e);
            }
        }
        return null;
    }
}
