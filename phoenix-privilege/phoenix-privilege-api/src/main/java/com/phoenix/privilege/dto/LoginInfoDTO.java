package com.phoenix.privilege.dto;

import lombok.Data;

@Data
public class LoginInfoDTO {
	/**
	 * 用户类型  user 普通用户  admin 管理员
	 */
	private String type;

	private String username;

	private String password;

	private String captchaKey;

	private String captchaCode;

}
