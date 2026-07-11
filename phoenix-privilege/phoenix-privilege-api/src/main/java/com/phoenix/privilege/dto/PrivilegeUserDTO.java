package com.phoenix.privilege.dto;

import com.mybatisflex.annotation.Table;
import com.phoenix.privilege.entity.PrivilegeUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("tbl_privilege_user")
public class PrivilegeUserDTO {

	public PrivilegeUser toEntity() {
		PrivilegeUser entity = new PrivilegeUser();
		BeanUtils.copyProperties(this, entity);
		return entity;
	}

	private String id;

	/** 人员ID */
	private String employeeId;

	private String code;

	private String realName;

	private String username;

	private String password;

	private String tel;

	private String phone;

	private String mobile;

	private String email;

	private byte[] image;

	private String companyId;

	private String deptId;

	private String itUserId;

	private String itUserName;

	private Integer isLeader;

	private Integer sex;

	private String address;

	private String fax;

	private Integer failMonth;

	private Date failureTime;

	private Integer aclTimestamp;

	private Date pwdFtime;

	private Integer pwdInit;

	private Integer userType;

	private String keyword;

	private String createBy;

}
