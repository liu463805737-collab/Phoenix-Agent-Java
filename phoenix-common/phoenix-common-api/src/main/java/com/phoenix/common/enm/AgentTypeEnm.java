package com.phoenix.common.enm;

import lombok.Getter;

@Getter
public enum AgentTypeEnm {
    SQL("sql", "数据智能体"), AGENT("agent", "智能体"), HARNESS("harness", "Harness"), WORKFLOW("workflow", "流程智能体");
    private final String code;
    private final String desc;

    AgentTypeEnm(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
