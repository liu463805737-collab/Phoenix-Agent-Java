package com.phoenix.common.enm;

import lombok.Getter;

/**
 * 平台类型
 */
@Getter
public enum PlatformTypeEnm {
    WEIXIN("weixin", "微信"), DINGTALK("dingtalk", "钉钉"), FEISHU("feishu", "飞书"),;

    private final String code;

    private final String desc;

    PlatformTypeEnm(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PlatformTypeEnm valueOfCode(String code) {
        for (PlatformTypeEnm type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

}
