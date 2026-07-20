package com.phoenix.agent.harness.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolCall {
    /**
     * 工具调用的唯一 ID（由大模型生成，回传时必须带上，用于匹配）
     */
    private String id;

    /**
     * 工具名称（例如 "refund_order"）
     */
    private String name;

    /**
     * 工具的参数（通常是一个 Map 或 JSON 字符串）
     */
    private Map<String, Object> arguments;
}
