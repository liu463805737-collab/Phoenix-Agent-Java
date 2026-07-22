package com.phoenix.agent.harness.request;

import io.agentscope.core.permission.PermissionRule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 接收前端人工干预确认请求的 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmRequest implements Serializable {
    private String userId;
    /**
     * 会话 ID，用于后端定位到具体的 Agent 实例和对应的 Flux 事件流
     */
    private String sessionId;
    private String agentSn;

    /**
     * 用户的决策：true 表示允许/确认，false 表示拒绝/取消
     */
    private boolean allowed;

    /**
     * 框架建议的权限规则。
     * 如果用户选择了“始终允许”，前端会将此规则原封不动回传；
     * 如果用户选择了“仅本次允许”或“拒绝”，该字段可为 null。
     */
    private List<PermissionRule> suggestedRules;
}
