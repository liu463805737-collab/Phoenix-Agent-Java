package com.phoenix.agent.harness.agent.human;

import com.phoenix.agent.harness.agent.AbstractHarnessAgent;
import com.phoenix.agent.harness.middleware.StopOnAllDeniedMiddleware;
import io.agentscope.core.permission.PermissionBehavior;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.core.permission.PermissionRule;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import io.agentscope.harness.agent.memory.compaction.ToolResultEvictionConfig;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
@Component
public class HumanInTheLoop extends AbstractHarnessAgent {
    @Override
    public String getSn() {
        return "HumanInTheLoop";
    }

    @Override
    public String getName() {
        return "测试人工干预";
    }

    @Override
    public String getDescription() {
        return "测试人工干预";
    }

    @Override
    public HarnessAgent createHarnessAgent() {
// 2. 注册工具
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new HumanOrderTools());

        // 3. 配置权限规则（核心：人工干预逻辑）
        PermissionContextState perms = PermissionContextState.builder()
                .mode(PermissionMode.DEFAULT)
                // 查订单：直接放行
                .addAllowRule("query_order", new PermissionRule(
                        "query_order", null, PermissionBehavior.ALLOW, "userSettings"))
                // 退款：弹窗询问用户（触发人工确认）
                .addAskRule("refund_order", new PermissionRule(
                        "refund_order", null, PermissionBehavior.ASK, "userSettings"))
                // 删表：直接拒绝
                .addDenyRule("drop_table", new PermissionRule(
                        "drop_table", null, PermissionBehavior.DENY, "userSettings"))
                .build();
        HarnessAgent agent = HarnessAgent.builder()
                .name(this.getSn())
                .sysPrompt("你是客服，只能调用 query_order、refund_order、drop_table 工具。")
                .model(this.createChatModel()) // 请替换为你实际的模型配置
                .workspace(Path.of("./workspace"))
                .toolkit(toolkit)
                .permissionContext(perms)
                .disableShellTool()
                .enablePlanMode(true)
                .distributedStore(redisDistributedStore)
                .stateStore(postgresAgentStateStore)
                .memory(this.getDefaultModelConfig())
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
