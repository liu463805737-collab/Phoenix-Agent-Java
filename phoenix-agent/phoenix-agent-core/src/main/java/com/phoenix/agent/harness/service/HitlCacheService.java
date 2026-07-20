package com.phoenix.agent.harness.service;

import io.agentscope.core.event.RequireUserConfirmEvent;

public interface HitlCacheService {
    /**
     * 保存用户确认的缓存信息
     * @param sessionId sessionId
     * @param event 用户确认事件
     */
    void savePendingConfirm(String sessionId, RequireUserConfirmEvent event);

    /**
     * 当收到前端确认请求时，从 Redis 取出并删除
     * @param sessionId sessionId
     * @return
     */
    RequireUserConfirmEvent getAndRemovePendingConfirm(String sessionId);
}
