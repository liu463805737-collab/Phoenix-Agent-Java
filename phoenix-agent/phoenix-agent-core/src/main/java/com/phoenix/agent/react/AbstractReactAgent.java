package com.phoenix.agent.react;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.summarization.SummarizationHook;
import com.alibaba.cloud.ai.graph.agent.interceptor.Interceptor;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.phoenix.common.enm.AgentTypeEnm;
import com.phoenix.agent.hook.CombinedDbHook;
import com.phoenix.agent.interceptor.LoginUserAgentInterceptor;
import com.phoenix.data.dto.ModelConfigDTO;
import com.phoenix.data.entity.Agent;
import com.phoenix.data.enums.AgentStatusEnm;
import com.phoenix.data.enums.ModelType;
import com.phoenix.data.service.agent.AgentService;
import com.phoenix.data.service.aimodelconfig.DynamicModelFactory;
import com.phoenix.data.service.aimodelconfig.ModelConfigDataService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 智能体抽象类
 */
public abstract class AbstractReactAgent implements SmartInitializingSingleton {
    @Autowired
    private AgentService agentService;
    @Autowired
    protected RedisSaver saver;
    @Autowired
    protected AgentStaticLoader agentStaticLoader;
    @Autowired
    protected LoginUserAgentInterceptor loginUserAgentInterceptor;
    @Autowired
    protected ModelCallLimitHook modelCallLimitHook;
    @Autowired
    protected CombinedDbHook patrolCombinedDbHook;
    @Autowired
    protected SummarizationHook summarizationHook;
    @Autowired
    protected ModelConfigDataService modelConfigDataService;
    @Autowired
    protected DynamicModelFactory modelFactory;

    /**
     * 创建ChatModel
     * @return ChatModel
     */
    public ChatModel createChatModel() {
        ModelConfigDTO config = modelConfigDataService.getActiveConfigByType(ModelType.CHAT);
        ChatModel chatModel = modelFactory.createChatModel(config);
        return chatModel;
    }

    /**
     * 获取Agent名称
     *
     * @return String
     */
    public abstract String getSn();

    /**
     * 智能体的名称
     * @return
     */
    public abstract String getName();

    /**
     * 智能体的描述
     * @return
     */
    public abstract String getDescription();

    /**
     * 注册智能体到容器里面
     */
    public void register(){
        ReactAgent reactAgent = createReactAgent();
        Agent agent = Agent.builder().sn(getSn()).type(AgentTypeEnm.AGENT.getCode()).name(getName()).status(AgentStatusEnm.DRAFT.getCode()).description(getDescription()).build();
        agentService.saveBySn(agent);
        agentStaticLoader.addAgent(getSn(), reactAgent);
    }

    /**
     * 创建管智能体
     * @return
     */
    public abstract ReactAgent createReactAgent();


    @Override
    public void afterSingletonsInstantiated() {
        this.register();
    }

    /**
     * 获取通用的hook
     *
     * @return
     */
    public List<Hook> getCommonHooks() {
        return List.of(summarizationHook, modelCallLimitHook, patrolCombinedDbHook);
    }


    /**
     * 获取通用的Interceptor
     *
     * @return
     */
    public List<Interceptor> getCommonInterceptors() {
        return List.of(loginUserAgentInterceptor);
    }
}
