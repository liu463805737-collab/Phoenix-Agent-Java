package com.phoenix.agent.workflow;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.summarization.SummarizationHook;
import com.alibaba.cloud.ai.graph.agent.interceptor.Interceptor;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
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

public abstract class AbstractCompiledGragph implements SmartInitializingSingleton {
    @Autowired
    protected CompiledGraphStaticLoader  compiledGraphStaticLoader;
    @Autowired
    protected RedisSaver saver;
    @Autowired
    protected SaverConfig saverConfig;
    @Autowired
    protected LoginUserAgentInterceptor loginUserAgentInterceptor;
    @Autowired
    protected ModelCallLimitHook modelCallLimitHook;
    @Autowired
    protected CombinedDbHook patrolCombinedDbHook;
    @Autowired
    protected SummarizationHook summarizationHook;
    @Autowired
    protected AgentService agentService;
    @Autowired
    protected ModelConfigDataService modelConfigDataService;
    @Autowired
    protected DynamicModelFactory modelFactory;
    /**
     * 获取Agent名称
     * @return String
     */
    public abstract  String getSn();
    public abstract String getName();
    public abstract String getDescription() ;

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
     * @return String
     */
    public void register() throws GraphStateException{
        CompiledGraph compiledGraph = createParolCompiledGraph();
        Agent agent = Agent.builder().sn(getSn()).type(AgentTypeEnm.WORKFLOW.getCode()).name(getName()).status(AgentStatusEnm.DRAFT.getCode()).description(getDescription()).build();
        agentService.saveBySn(agent);
        compiledGraphStaticLoader.addCompiledGraph(getSn(), compiledGraph);
    }

    public abstract CompiledGraph createParolCompiledGraph() throws GraphStateException;

    public CompileConfig buildCompileConfig() {
        return CompileConfig.builder().saverConfig(saverConfig).build();
    }

    @Override
    public void afterSingletonsInstantiated() {
        try {
            this.register();
        } catch (GraphStateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取通用的hook
     * @return
     */
    public List<Hook> getCommonHooks() {
        return List.of(summarizationHook, modelCallLimitHook,  patrolCombinedDbHook);
    }


    /**
     * 获取通用的Interceptor
     * @return
     */
    public List<Interceptor> getCommonInterceptors() {
        return List.of(loginUserAgentInterceptor);
    }
}
