package com.phoenix.agent.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenix.agent.harness.request.ConfirmRequest;
import com.phoenix.agent.harness.request.HarnessRequest;
import com.phoenix.agent.harness.send.HarnessChatService;
import com.phoenix.agent.harness.service.HitlCacheService;
import com.phoenix.privilege.entity.PrivilegeUser;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.RequireUserConfirmEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.phoenix.privilege.constant.CommonConstant.LOGIN_USER_INFO;

/**
 * @author burce.liu
 */
@Slf4j
@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/admin/harness")
public class HarnessController {
    private final HarnessChatService harnessChatService;
    private final HitlCacheService hitlCacheService;

    @PostMapping(value = "/confirm", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Object>> confirm(@RequestBody ConfirmRequest confirmRequest) {
        String userId = StpUtil.getLoginIdAsString();
        confirmRequest.setUserId(userId);
        return harnessChatService.confirmStream(confirmRequest.getAgentSn(), confirmRequest).map(output-> {
            Map<String, Object> eventMap = new LinkedHashMap<>();
            eventMap.put("content", "");
            if (output instanceof StreamingOutput<?> streamingOutput && streamingOutput.chunk() != null) {
                eventMap.put("content", streamingOutput.chunk());
            }
            return eventMap;
        });
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Object>> harnessChat(@RequestBody HarnessRequest  harnessRequest) {
        String userId = StpUtil.getLoginIdAsString();
        HarnessRequest request = HarnessRequest.builder().userId(userId).sessionId(harnessRequest.getSessionId()).message(harnessRequest.getMessage()).build();
        return harnessChatService.stream(harnessRequest.getHarnessSn(), request)
                .map(output -> {
                    Map<String, Object> eventMap = new LinkedHashMap<>();
                    eventMap.put("content", "");
                    eventMap.put("end", false);
                    if (output instanceof StreamingOutput<?> streamingOutput && streamingOutput.chunk() != null) {
                        eventMap.put("content", streamingOutput.chunk());
                    }
                    if (output.isEND()) {
                        eventMap.put("end", true);
                    }
                    output.state().value("agent_event", AgentEvent.class).ifPresent(event -> {
                        if (event instanceof RequireUserConfirmEvent confirmEvent) {
                            hitlCacheService.savePendingConfirm(harnessRequest.getHarnessSn(), confirmEvent);
                        }
                    });
                    return eventMap;
                });
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
