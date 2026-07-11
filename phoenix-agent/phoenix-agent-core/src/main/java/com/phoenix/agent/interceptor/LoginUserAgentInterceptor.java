package com.phoenix.agent.interceptor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import com.phoenix.agent.model.UserAgentInfo;
import com.phoenix.agent.service.UserAgentInfoService;
import com.phoenix.agent.service.vectorstore.UserMemoryVectorService;
import com.phoenix.common.vectorstore.request.SearchVectorRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.phoenix.agent.constant.PhoenixAgentConstant.COMMON_REDIS_KEY;


/**
 * 用户调用次数拦截器
 */
@Slf4j
@Component
public class LoginUserAgentInterceptor extends ModelInterceptor {
    @Autowired
    private UserAgentInfoService userAgentInfoService;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserMemoryVectorService userMemoryVectorService;

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        Map<String, Object> context = request.getContext();
        String userId = (String) context.get("userId");
        String sessionId = (String) context.get("sessionId");
        String agentSn = (String) context.get("agentSn");
        String redisKey = redisKey(userId, sessionId);
        String exist = stringRedisTemplate.opsForValue().get(redisKey);
        if (StrUtil.isEmpty(exist)) {
            stringRedisTemplate.opsForValue().set(redisKey, agentSn, 1, TimeUnit.MINUTES);
        } else {
            return handler.call(request);
        }
        UserAgentInfo userAgentInfo = new UserAgentInfo();
        userAgentInfo.setUserId(userId);
        userAgentInfo.setAgentSn(agentSn);
        CompletableFuture.runAsync(() -> {
            transactionTemplate.execute(status -> {
                userAgentInfoService.recordAction(userAgentInfo);
                return true;
            });
        }).exceptionally(ex -> {
            log.error("异步保存调用记录失败", ex);
            return null;
        });
        this.addHistoryMemory(userId, agentSn, request);
        return handler.call(request);
    }

    @Override
    public String getName() {
        return "LoginUserAgentInterceptor";
    }

    private String redisKey(String userId, String sessionId) {
        return StrUtil.join(":", COMMON_REDIS_KEY, userId, sessionId);
    }

    private void addHistoryMemory(String userId, String agentSn, ModelRequest request) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        var userIdOp = builder.eq("userId", userId);
        var agentSnOp = builder.eq("agentSn", agentSn);
        Map<String, Object> context = request.getContext();
        String input = (String) context.get("input");
        Filter.Expression filterExpression = builder.and(userIdOp, agentSnOp).build();
        SearchVectorRequest searchVectorRequest = SearchVectorRequest.builder().query(input).topK(5).threshold(0.6).filter(filterExpression).build();
        List<Document> documents = userMemoryVectorService.searchUserMemoryDocumentKnowledge(searchVectorRequest);
        StringBuilder sb = new StringBuilder();
        if (!documents.isEmpty()) {
            sb.append("【相关历史记忆】\n");
            for (Document doc : documents) {
                sb.append("- ").append(doc.getText()).append("\n");
            }
        }
        if (sb.length() > 0) {
            SystemMessage historySystemMessage = SystemMessage.builder().text(sb.toString()).build();
            request.getMessages().add(historySystemMessage);
        }
    }
}
