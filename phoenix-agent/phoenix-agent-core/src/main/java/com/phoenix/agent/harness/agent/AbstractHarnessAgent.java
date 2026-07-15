package com.phoenix.agent.harness.agent;

import com.phoenix.agent.service.harness.HarnessModelRegistry;
import com.phoenix.common.enm.AgentTypeEnm;
import com.phoenix.data.entity.Agent;
import com.phoenix.data.enums.AgentStatusEnm;
import com.phoenix.data.service.agent.AgentService;
import io.agentscope.core.skill.repository.postgresql.PostgresSkillRepository;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.postgresql.state.PostgresAgentStateStore;
import io.agentscope.extensions.redis.RedisDistributedStore;
import io.agentscope.extensions.redis.store.RedisStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.MemoryConfig;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.Duration;

/**
 * 智能体抽象类
 */
public abstract class AbstractHarnessAgent implements SmartInitializingSingleton {
    @Autowired
    private AgentService agentService;
    @Autowired
    private HarnessModelRegistry harnessModelRegistry;
    @Autowired
    private HarnessStaticLoader harnessStaticLoader;
    @Autowired
    protected RedisDistributedStore redisDistributedStore;
    @Autowired
    protected PostgresAgentStateStore postgresAgentStateStore;
    @Autowired
    protected PostgresSkillRepository postgresSkillRepository;
    @Autowired
    @Qualifier("harnessRedisStore")
    protected RedisStore redisStore;

    public MemoryConfig getDefaultModelConfig() {
        return MemoryConfig.builder()
                .model(this.createChatModel())
                .consolidationMaxTokens(2000)
                .consolidationMinGap(Duration.ofMinutes(60))
                .dailyFileRetentionDays(30)
                .sessionRetentionDays(90)
                .flushTrigger(MemoryConfig.FlushTrigger.throttled(Duration.ofSeconds(30)))
                .build();
    }

    /**
     * 创建ChatModel
     *
     * @return ChatModel
     */
    public OpenAIChatModel createChatModel() {
        return harnessModelRegistry.getOpenAIChatModel();
    }

    /**
     * 获取Agent名称
     *
     * @return String
     */
    public abstract String getSn();

    /**
     * 智能体的名称
     *
     * @return
     */
    public abstract String getName();

    /**
     * 智能体的描述
     *
     * @return
     */
    public abstract String getDescription();

    /**
     * 注册智能体到容器里面
     */
    public void register() {
        HarnessAgent harnessAgent = createHarnessAgent();
        Agent agent = Agent.builder().sn(getSn()).type(AgentTypeEnm.HARNESS.getCode()).name(getName()).status(AgentStatusEnm.DRAFT.getCode()).description(getDescription()).build();
        agentService.saveBySn(agent);
        harnessStaticLoader.addAgent(getSn(), harnessAgent);
    }

    /**
     * 创建管智能体
     *
     * @return
     */
    public abstract HarnessAgent createHarnessAgent();


    @Override
    public void afterSingletonsInstantiated() {
        this.register();
    }

}
