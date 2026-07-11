package com.phoenix.privilege.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {

	DISABLE(0, "禁用"), ENABLE(1, "启用"), CANCELLED(2, "已注销"),;

	private final Integer code;

	private final String desc;

	UserStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static String getByCode(Integer code) {
		for (UserStatusEnum value : values()) {
			if (value.code.equals(code)) {
				return value.desc;
			}
		}
		return null;
	}

	public static UserStatusEnum valueOf(Integer code) {
		for (UserStatusEnum value : values()) {
			if (value.code.equals(code)) {
				return value;
			}
		}
		return null;
	}

}
