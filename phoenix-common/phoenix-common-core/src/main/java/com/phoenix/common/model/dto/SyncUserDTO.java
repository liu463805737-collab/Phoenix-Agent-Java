package com.phoenix.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncUserDTO {

	private String thirdPartyId;

	private String username;

	private String realName;

	private String phone;

	private String email;

	private String avatarUrl;

	private String deptId;

	private String deptName;

	private String thirdUnionId;

	private String thirdOpenId;

	private String thirdUserId;

	/** 是否在职：0离职 1在职 */
	private Integer status;

}
