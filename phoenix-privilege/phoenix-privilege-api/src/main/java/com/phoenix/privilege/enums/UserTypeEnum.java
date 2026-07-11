package com.phoenix.privilege.enums;

import lombok.Getter;

@Getter
public enum UserTypeEnum {

	ADMIN_USER(0, "管理员"), COMMON_USER(1, "普通用户"),;

	private final Integer code;

	private final String desc;

	UserTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static String getByCode(Integer code) {
		for (UserTypeEnum value : values()) {
			if (value.code.equals(code)) {
				return value.desc;
			}
		}
		return null;
	}

	public static UserTypeEnum valueOf(Integer code) {
		for (UserTypeEnum value : values()) {
			if (value.code.equals(code)) {
				return value;
			}
		}
		return null;
	}

}
