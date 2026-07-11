package com.phoenix.agent.utils;

import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;

/**
 * // 使用示例
 * InterruptionMetadata approvalMetadata = HITLHelper.approveAll(interruptionMetadata);
 * InterruptionMetadata rejectMetadata = HITLHelper.rejectAll(interruptionMetadata, "操作不安全");
 * InterruptionMetadata editMetadata = HITLHelper.editTool(
 *   interruptionMetadata,
 *   "execute_sql",
 *   "{"query": "SELECT * FROM records LIMIT 10"}"
 * );
 */
public class HITLHelper {
    /**
     * 批准所有工具调用
     */
    public static InterruptionMetadata approveAll(InterruptionMetadata interruptionMetadata) {
        InterruptionMetadata.Builder builder = InterruptionMetadata.builder()
                .nodeId(interruptionMetadata.node())
                .state(interruptionMetadata.state());

        interruptionMetadata.toolFeedbacks().forEach(toolFeedback -> {
            builder.addToolFeedback(
                    InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                            .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
                            .build()
            );
        });

        return builder.build();
    }

    /**
     * 拒绝所有工具调用
     */
    public static InterruptionMetadata rejectAll(
            InterruptionMetadata interruptionMetadata,
            String reason) {
        InterruptionMetadata.Builder builder = InterruptionMetadata.builder()
                .nodeId(interruptionMetadata.node())
                .state(interruptionMetadata.state());

        interruptionMetadata.toolFeedbacks().forEach(toolFeedback -> {
            builder.addToolFeedback(
                    InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                            .result(InterruptionMetadata.ToolFeedback.FeedbackResult.REJECTED)
                            .description(reason)
                            .build()
            );
        });

        return builder.build();
    }

    /**
     * 编辑特定工具的参数
     */
    public static InterruptionMetadata editTool(
            InterruptionMetadata interruptionMetadata,
            String toolName,
            String newArguments) {
        InterruptionMetadata.Builder builder = InterruptionMetadata.builder()
                .nodeId(interruptionMetadata.node())
                .state(interruptionMetadata.state());

        interruptionMetadata.toolFeedbacks().forEach(toolFeedback -> {
            if (toolFeedback.getName().equals(toolName)) {
                builder.addToolFeedback(
                        InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                                .arguments(newArguments)
                                .result(InterruptionMetadata.ToolFeedback.FeedbackResult.EDITED)
                                .build()
                );
            } else {
                builder.addToolFeedback(
                        InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                                .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
                                .build()
                );
            }
        });

        return builder.build();
    }
}
