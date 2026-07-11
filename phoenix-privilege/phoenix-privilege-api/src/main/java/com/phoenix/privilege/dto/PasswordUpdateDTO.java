package com.phoenix.privilege.dto;

import lombok.Data;

@Data
public class PasswordUpdateDTO {

	private String userId;

	private String oldPassword;

	private String newPassword;

}
