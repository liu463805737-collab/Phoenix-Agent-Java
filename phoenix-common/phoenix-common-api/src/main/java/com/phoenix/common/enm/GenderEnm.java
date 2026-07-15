package com.phoenix.common.enm;

import lombok.Getter;

@Getter
public enum GenderEnm {
    MALE("1", "男"), FEMALE("0", "女"), UNKNOWN("-1", "未知"),;

    private final String code;

    private final String desc;

    GenderEnm(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static GenderEnm valueOfCode(String code) {
        for (GenderEnm type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

}
