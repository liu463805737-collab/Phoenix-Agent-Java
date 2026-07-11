package com.phoenix.common.exception;


import lombok.Getter;

@Getter
public enum ExceptionAgentEnum {


    AGENT_STATUS_DISABLED(410, "改智能体已禁用，请联系管理员！")
        ;



    private final int code;
    private final String message;

    ExceptionAgentEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
