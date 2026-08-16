package com.phoenix.agent.harness.agent.human;

import cn.hutool.core.util.StrUtil;
import com.phoenix.agent.harness.agent.AbstractHarnessAgent;
import com.phoenix.agent.harness.middleware.StopOnAllDeniedMiddleware;
import com.phoenix.data.entity.Agent;
import com.phoenix.data.service.agent.AgentService;
import io.agentscope.core.permission.PermissionBehavior;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.core.permission.PermissionRule;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import io.agentscope.harness.agent.memory.compaction.ToolResultEvictionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
@Component
public class HumanInTheLoop extends AbstractHarnessAgent {


    @Autowired
    private AgentService agentService;
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
        Agent agent = agentService.findBySn(getSn());
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
        String sysPrompt = """
            你是智能客服助手，具备以下能力：
            
            1. **查询订单 (query_order)** - 当用户问"我的订单"、"快递到哪了"、"订单状态"时调用
               → 执行前要说："正在查询您的订单，请稍候..."
            
            2. **处理退款 (refund_order)** - 当用户说"退货"、"退款"、"不要了"时调用
               → 执行前要说："收到您的退款申请，正在为您处理..."
            
            3. **数据库管理 (drop_table)** - 需要管理员权限的高危操作
               → 必须询问："此操作不可恢复，请确认是否继续？"
            
            【重要规则】
            - 永远用中文回答
            - 永远用自然语言向用户解释你要做什么
            - 不要告诉用户"我要调用xxx工具"，而是用"我正在为您..."的表达
            - 如果用户的输入不明确，先追问再行动
            - 用户发"wwwwwwwww"等无意义输入时，友善地引导："您好，请问有什么可以帮您的吗？"
            """;

        String sytemPrompt = null;
        if (agent != null) {
            sytemPrompt = StrUtil.blankToDefault(agent.getPrompt(), sysPrompt);
        }

        HarnessAgent harnessAgent = HarnessAgent.builder()
                .name(this.getSn())
                .description(agent.getDescription())
                .sysPrompt(sytemPrompt)
                .model(this.createChatModel()) // 请替换为你实际的模型配置
                .workspace(Path.of(".agentscope/workspace"))
                .filesystem(pgRemoteFilesystemSpec)
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
        return harnessAgent;
    }
}
