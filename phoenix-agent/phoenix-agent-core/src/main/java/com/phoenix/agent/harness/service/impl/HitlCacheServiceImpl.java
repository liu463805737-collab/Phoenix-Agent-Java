package com.phoenix.agent.harness.service.impl;

import com.phoenix.agent.harness.service.HitlCacheService;
import io.agentscope.core.event.RequireUserConfirmEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class HitlCacheServiceImpl implements HitlCacheService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PENDING_CONFIRM_KEY = "phoenix:agentscope:session:%s:pending_confirm";


    @Override
    public void savePendingConfirm(String sessionId, RequireUserConfirmEvent event) {
        String key = String.format(PENDING_CONFIRM_KEY, sessionId);
        String json = objectMapper.writeValueAsString(event);
        redisTemplate.opsForValue().set(key, json, 30, TimeUnit.MINUTES);
    }

    @Override
    public RequireUserConfirmEvent getAndRemovePendingConfirm(String sessionId) {
        String key = String.format(PENDING_CONFIRM_KEY, sessionId);
        String json = redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(json)) {
            RequireUserConfirmEvent obj = objectMapper.readValue(json, RequireUserConfirmEvent.class);
            redisTemplate.delete(key); // 取出后立刻删除，防止重复确认
            return obj;
        }
        return null;
    }
}
