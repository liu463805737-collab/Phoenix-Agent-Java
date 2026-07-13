package com.phoenix.agent.harness.agent;

import com.phoenix.agent.service.harness.HarnessModelRegistry;
import io.agentscope.core.skill.repository.postgresql.PostgresSkillRepository;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.builtin.TodoTools;
import io.agentscope.extensions.postgresql.state.PostgresAgentStateStore;
import io.agentscope.extensions.redis.RedisDistributedStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import io.agentscope.harness.agent.memory.compaction.ToolResultEvictionConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;


@Component
@RequiredArgsConstructor
public class RulesReactAgent {
    private final HarnessModelRegistry harnessModelRegistry;
    private final RedisDistributedStore redisDistributedStore;
    private final PostgresAgentStateStore postgresAgentStateStore;
    private final PostgresSkillRepository  postgresSkillRepository;
    private final RulesRagTool rulesRagTool;

    public HarnessAgent createReActAgent() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new TodoTools());
        toolkit.registerTool(rulesRagTool);
        HarnessAgent agent = HarnessAgent.builder()
                .name("Assistant")
                .sysPrompt("""
                        你是一个制度查询专家，你所有的查询结果都强依赖tool返回的结果，不可篡改。
                        """)
                .model(harnessModelRegistry.getOpenAIChatModel())
                .toolkit(toolkit)
                .enablePlanMode(true)
                .stateStore(postgresAgentStateStore)
                .distributedStore(redisDistributedStore)
                .skillRepository(postgresSkillRepository)
                .workspace(Paths.get(".agentscope/workspace"))
                .enablePendingToolRecovery(true)
                .compaction(CompactionConfig.builder().triggerMessages(50)      // 50 条触发摘要压缩
                        .truncateArgs(CompactionConfig.TruncateArgsConfig.builder()
                                .maxArgLength(2000)
                                .truncationText("... [truncated] ...")
                                .build())
                        .keepMessages(20)         // 保留尾部 20 条原文
                        .build())
                //当某条工具结果文本超过阈值(默认 80K 字符 ≈ 20K tokens),把全文写到工作区某个目录,上下文里只保留首尾各约 2K 字符 + 一个 read_file 路径提示符
                .toolResultEviction(ToolResultEvictionConfig.defaults())
                .build();
        return agent;
    }
}
