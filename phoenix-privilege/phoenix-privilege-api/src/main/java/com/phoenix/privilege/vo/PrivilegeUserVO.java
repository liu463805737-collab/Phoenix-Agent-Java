package com.phoenix.privilege.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.ColumnMask;
import com.mybatisflex.core.mask.Masks;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeUserVO {

	private String id;

	/** 人员ID */
	private String employeeId;

	private String code;

	private String realName;

	private String username;
	@ColumnMask(Masks.PASSWORD)
	private String password;

	private String tel;

	private String phone;

	private String mobile;

	private String email;

	private byte[] image;

	private String companyId;

	private String companyName;

	private String deptId;

	private String deptName;

	private Long itUserId;

	private String itUserName;

	private Integer isLeader;

	private Integer sex;

	private String address;

	private String fax;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;

	private String creator;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;

	private Integer failMonth;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date failureTime;

	private Integer aclTimestamp;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date pwdFtime;

	private Integer pwdInit;

	private String updator;

	private Integer delFlag;

	private Integer userType;

	/** 人员禁用状态 1是0否 */
	private Integer status;

}
