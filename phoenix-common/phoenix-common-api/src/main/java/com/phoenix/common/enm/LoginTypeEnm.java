package com.phoenix.common.enm;

import lombok.Getter;

/**
 * 用户类型
 */
@Getter
public enum LoginTypeEnm {
    USER("user", "前台用户"), ADMIN("admin", "后台用户");

    private final String code;

    private final String desc;

    LoginTypeEnm(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static LoginTypeEnm valueOfCode(String code) {
        for (LoginTypeEnm type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

}
