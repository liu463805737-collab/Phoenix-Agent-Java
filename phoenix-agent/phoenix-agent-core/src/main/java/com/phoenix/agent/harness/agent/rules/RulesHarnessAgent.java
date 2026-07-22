package com.phoenix.agent.harness.agent.rules;

import com.phoenix.agent.harness.agent.AbstractHarnessAgent;
import com.phoenix.agent.harness.middleware.StopOnAllDeniedMiddleware;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.builtin.TodoTools;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.IsolationScope;
import io.agentscope.harness.agent.filesystem.spec.RemoteFilesystemSpec;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import io.agentscope.harness.agent.memory.compaction.ToolResultEvictionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;


@Component
public class RulesHarnessAgent extends AbstractHarnessAgent {
    @Autowired
    private RulesRagTool rulesRagTool;
    @Override
    public String getSn() {
        return "RulesHarnessAgent";
    }

    @Override
    public String getName() {
        return "制度专家";
    }

    @Override
    public String getDescription() {
        return "专业查询制度的专家";
    }

    @Override
    public HarnessAgent createHarnessAgent() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new TodoTools());
        toolkit.registerTool(rulesRagTool);
        HarnessAgent agent = HarnessAgent.builder()
                .name(getSn())
                .sysPrompt("""
                        你是一个制度查询专家，你所有的查询结果都强依赖tool返回的结果，不可篡改。
                        """)
                .model(this.createChatModel())
                .toolkit(toolkit)
                .enablePlanMode(true)
                .disableShellTool()
                .distributedStore(redisDistributedStore)
                .stateStore(postgresAgentStateStore)
                .skillRepository(postgresSkillRepository)
                .filesystem(new RemoteFilesystemSpec(redisStore).isolationScope(IsolationScope.USER))
                .memory(this.getDefaultModelConfig())
                .workspace(Paths.get(".agentscope/workspace"))
                .enablePendingToolRecovery(true)
                .middleware(new StopOnAllDeniedMiddleware())
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
