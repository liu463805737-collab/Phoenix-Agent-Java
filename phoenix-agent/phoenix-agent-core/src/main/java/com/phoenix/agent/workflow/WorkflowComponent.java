package com.phoenix.agent.workflow;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.phoenix.agent.service.vectorstore.PostgresqlCombinedStore;
import com.phoenix.common.vo.login.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.phoenix.agent.constant.PhoenixAgentConstant.RUNTIMECONTEXT_USERPROFILE;


@Component
@RequiredArgsConstructor
public class WorkflowComponent {
    private final CompiledGraphStaticLoader compiledGraphStaticLoader;
    private final PostgresqlCombinedStore store;

    public String generatePlantUmlFromGraph(String name) {
        var compiledGraph = compiledGraphStaticLoader.getCompiledGraph(name);
        GraphRepresentation representation = compiledGraph.getGraph(
                GraphRepresentation.Type.PLANTUML,
                name
        );
        return representation.content();
    }

    /**
     * 人工干预的时候会使用这个方法
     * @param sn sn
     * @param message message
     * @param userProfile userProfile
     * @return
     * @throws GraphRunnerException
     */
    public Optional<NodeOutput> invokeAndGetOutput(String sn, String message, UserProfile userProfile) {
        Map<String, Object> input = new HashMap<>();
        input.put("input", message);
        input.put(RUNTIMECONTEXT_USERPROFILE, userProfile);
        RunnableConfig config = this.buildConfig(sn, userProfile, message);
        CompiledGraph compiledGraph = compiledGraphStaticLoader.getCompiledGraph(sn);
        return compiledGraph.invokeAndGetOutput(input, config);
    }

    public Flux<NodeOutput> streamCall(String name, String message, UserProfile userProfile) {
        CompiledGraph compiledGraph = compiledGraphStaticLoader.getCompiledGraph(name);
        RunnableConfig runnableConfig = buildConfig(name, userProfile, message);
        Map<String, Object> input = new HashMap<>();
        input.put("input", message);
        input.put(RUNTIMECONTEXT_USERPROFILE, userProfile);
        return compiledGraph.stream(input, runnableConfig);
    }


    /**
     * 构建运行时配置
     *
     * @param userProfile 登录信息
     * @return
     */
    private RunnableConfig buildConfig(String name, UserProfile userProfile,String message) {
        RunnableConfig.Builder builder = RunnableConfig.builder().threadId(userProfile.getSessionId());
        builder.addMetadata("userId", userProfile.getUserId())
                .addMetadata("sessionId", userProfile.getSessionId())
                .addMetadata("agentSn", name)
                .addMetadata("message", message);
        /**
         * 创建长期记忆
         */
        Map<String, Object> userInfo = BeanUtil.beanToMap(userProfile, false, true);
        //设置长期记忆
        StoreItem profileItem = StoreItem.of(List.of("profiles"), userProfile.getUserId(), userInfo);
        store.putItem(profileItem);
        builder.store(store);
        return builder.build();
    }

}
