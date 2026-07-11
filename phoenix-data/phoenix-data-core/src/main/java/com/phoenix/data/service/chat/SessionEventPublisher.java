package com.phoenix.data.service.chat;

import com.phoenix.data.vo.SessionUpdateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 会话事件推送器，通过 SSE 流向前端推送会话更新，一个 Agent 对应一个共享的 Sink。
 */
@Slf4j
@Service
public class SessionEventPublisher {

    private final Map<Integer, AgentSessionSink> sinks = new ConcurrentHashMap<>();

    /**
     * 注册 SSE 订阅，返回包含心跳的 Flux
     */
    public Flux<ServerSentEvent<SessionUpdateEvent>> register(Integer agentId) {
        AgentSessionSink sink = sinks.computeIfAbsent(agentId, id -> new AgentSessionSink());
        Flux<ServerSentEvent<SessionUpdateEvent>> heartbeat = Flux.interval(Duration.ofSeconds(2))
                .map(i -> ServerSentEvent.<SessionUpdateEvent>builder().comment("heartbeat").build());
        sink.increment();
        log.debug("Registered subscriber for agent {}, current count: {}", agentId, sink.subscribers.get());
        return Flux.merge(heartbeat, sink.sink.asFlux()).doFinally(signalType -> cleanup(agentId, sink, signalType));
    }

    /**
     * 推送会话标题更新事件
     */
    public void publishTitleUpdated(Integer agentId, String sessionId, String title) {
        if (agentId == null) {
            return;
        }
        SessionUpdateEvent event = SessionUpdateEvent.titleUpdated(sessionId, title);
        AgentSessionSink sink = sinks.get(agentId);
        if (sink == null) {
            log.debug("No active subscribers for agent {}, skip pushing session title update", agentId);
            return;
        }
        Sinks.EmitResult result = sink.sink.tryEmitNext(ServerSentEvent.builder(event).event(event.getType()).build());
        if (result.isFailure()) {
            log.warn("Failed to emit session title update for agent {}, session {}, reason {}", agentId, sessionId,
                    result);
        }
    }

    /**
     * 清理订阅资源，无订阅者时移除 Sink
     */
    private void cleanup(Integer agentId, AgentSessionSink sink, SignalType signalType) {
        int current = sink.decrement();
        log.debug("Cleanup called for agent {}, signal: {}, remaining subscribers: {}", agentId, signalType, current);
        if (current <= 0) {
            // 使用 remove(key, value) 确保只移除当前的 sink 实例，防止并发问题
            if (sinks.remove(agentId, sink)) {
                sink.sink.tryEmitComplete();
                log.debug("Removed session update sink for agent {}", agentId);
            }
        }
    }

    /**
     * Agent 会话 Sink，包含订阅计数和 SSE 多播 Sink
     */
    private static class AgentSessionSink {

        private final AtomicInteger subscribers = new AtomicInteger(0);

        private final Sinks.Many<ServerSentEvent<SessionUpdateEvent>> sink = Sinks.many()
                .multicast()
                .onBackpressureBuffer();

        /**
         * 增加订阅计数
         */
        private void increment() {
            subscribers.incrementAndGet();
        }

        /**
         * 减少订阅计数并返回当前值
         */
        private int decrement() {
            return subscribers.decrementAndGet();
        }

    }

}
