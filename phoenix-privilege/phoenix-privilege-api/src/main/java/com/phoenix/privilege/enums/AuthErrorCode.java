package com.phoenix.privilege.enums;

import lombok.Getter;

@Getter
public enum AuthErrorCode {

	CAPTCHA_EXPIRED(23001, "验证码已失效，请重新获取"),
	CAPTCHA_INVALID(23002, "验证码错误"),
	LOGIN_FAIL(23003, "用户名或密码错误"),
	USER_DISABLED(23004, "用户已被禁用"),
	USERNAME_EXIST(23005, "用户名已存在"),
	MOBILE_EXIST(23006, "手机号已存在"),
	PASSWORD_ERROR(23009, "密码错误"),
	OLD_PASSWORD_ERROR(23007, "原密码错误"),
	RESET_PASSWORD_SAME(23008, "新密码不能与旧密码相同"),
	TOKEN_INVALID(401, "未授权，请先登录");

	private final int code;

	private final String message;

	AuthErrorCode(int code, String message) {
		this.code = code;
		this.message = message;
	}

}
