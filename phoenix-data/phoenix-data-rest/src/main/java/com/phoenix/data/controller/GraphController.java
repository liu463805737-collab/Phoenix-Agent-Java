package com.phoenix.data.controller;

import com.phoenix.data.dto.GraphRequest;
import com.phoenix.data.service.graph.GraphService;
import com.phoenix.data.vo.GraphNodeResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import static com.phoenix.data.constant.Constant.STREAM_EVENT_COMPLETE;
import static com.phoenix.data.constant.Constant.STREAM_EVENT_ERROR;

/**
 * 图表流式搜索控制器，提供SSE流式响应接口
 *
 * @author zhangshenghang
 * @author vlsmb
 */
@Slf4j
@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class GraphController {

    private final GraphService graphService;

    /**
     * SSE 心跳保活间隔，防止长报告生成期间连接被空闲超时断开
     */
    private static final java.time.Duration SSE_KEEP_ALIVE_INTERVAL = java.time.Duration.ofSeconds(20);

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
    @GetMapping(value = "/stream/search", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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

        // share() 将 unicast sink 转成热发布，供合并的心跳流复用同一份数据源
        Flux<ServerSentEvent<GraphNodeResponse>> stream = sink.asFlux().filter(sse -> {
                    // 1. 如果 event 是 "complete" 或 "error"，直接放行（不管 text 是否为空）
                    if (STREAM_EVENT_COMPLETE.equals(sse.event()) || STREAM_EVENT_ERROR.equals(sse.event())) {
                        return true;
                    }
                    // 判断字符串是否为空
                    return sse.data() != null && sse.data().getText() != null && !sse.data().getText().isEmpty();
                })
                .share();

        // 心跳保活：报告生成期间可能长时间无数据推送，定时发送 SSE 注释，避免代理/服务端空闲超时断开连接
        Flux<ServerSentEvent<GraphNodeResponse>> heartbeat = Flux.interval(SSE_KEEP_ALIVE_INTERVAL)
                .map(i -> ServerSentEvent.<GraphNodeResponse>builder().comment("keep-alive").build())
                .takeUntilOther(stream.then());

        return Flux.merge(stream, heartbeat)
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

}
