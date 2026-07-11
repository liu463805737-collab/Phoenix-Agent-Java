package com.phoenix.agent.react;

import cn.hutool.core.map.MapUtil;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.phoenix.agent.model.UserProfileInfo;
import com.phoenix.agent.service.MemoryPipelineService;
import com.phoenix.agent.service.UserProfileInfoService;
import com.phoenix.agent.service.vectorstore.PostgresqlCombinedStore;
import com.phoenix.common.vo.login.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ReactAgentComponent 组件
 */
@Component
@RequiredArgsConstructor
public class ReactAgentComponent {
    private final AgentStaticLoader agentStaticLoader;
    private final PostgresqlCombinedStore store;
    private final MemoryPipelineService memoryPipelineService;
    private final UserProfileInfoService userProfileInfoService;

    /**
     * 人工干预的时候会使用这个方法
     *
     * @param sn          sn
     * @param message     message
     * @param userProfile userProfile
     * @return
     * @throws GraphRunnerException
     */
    public Optional<NodeOutput> invokeAndGetOutput(String sn, String message, UserProfile userProfile) throws GraphRunnerException {
        UserMessage userMessage = UserMessage.builder().text(message).build();
        RunnableConfig config = this.buildConfig(sn, userProfile, message);
        ReactAgent reactAgent = agentStaticLoader.loadAgent(sn);
        Optional<NodeOutput> nodeOutput = reactAgent.invokeAndGetOutput(userMessage, config);
        memoryPipelineService.processAndExtractMemory(userProfile.getUserId(), sn, message);
        return nodeOutput;
    }


    /**
     * 流式调用agent
     *
     * @param sn          agent标识
     * @param message     用户消息
     * @param userProfile 用户信息
     * @return
     * @throws Exception
     */
    public Flux<NodeOutput> stream(String sn, String message, UserProfile userProfile) throws Exception {
        UserMessage userMessage = UserMessage.builder().text(message).build();
        RunnableConfig config = this.buildConfig(sn, userProfile, message);
        ReactAgent reactAgent = agentStaticLoader.loadAgent(sn);
        Flux<NodeOutput> stream = reactAgent.stream(userMessage, config);
        memoryPipelineService.processAndExtractMemory(userProfile.getUserId(), sn, message);
        return stream;
    }


    /**
     * 构建运行时配置
     *
     * @param userProfile 登录信息
     * @return
     */
    private RunnableConfig buildConfig(String sn, UserProfile userProfile, String message) {
        RunnableConfig.Builder builder = RunnableConfig.builder().threadId(userProfile.getSessionId());
        builder.addMetadata("userId", userProfile.getUserId()).addMetadata("userCode", userProfile.getUserCode()).addMetadata("sessionId", userProfile.getSessionId()).addMetadata("agentSn", sn).addMetadata("message", message);
        /**
         * 创建用户信息的长期记忆
         */
        UserProfileInfo userProfileInfo = userProfileInfoService.getByUserIdAndAgentSn(userProfile.getUserId(), sn);
        if (userProfileInfo != null && MapUtil.isNotEmpty(userProfileInfo.getProfileData())) {
            Map<String, String> profileData = userProfileInfo.getProfileData();
            Map<String, Object> userProfileData = new HashMap<>();
            userProfileData.putAll(profileData);
            //设置长期记忆
            StoreItem profileItem = StoreItem.of(List.of(sn, userProfile.getUserId()), userProfile.getUserId(), userProfileData);
            store.putItem(profileItem);
        }
        builder.store(store);
        return builder.build();
    }
}
