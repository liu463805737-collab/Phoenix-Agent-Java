package com.phoenix.privilege.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.phoenix.privilege.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table("tbl_privilege_user")
public class PrivilegeUser extends BaseEntity {

	/** 人员ID */
	private String employeeId;

	/** 工号 */
	private String code;

	/** 真实姓名 */
	private String realName;

	/** 用户名 */
	private String username;

	/** 密码 */
	private String password;

	/** 电话 */
	private String tel;

	/** 座机 */
	private String phone;

	/** 手机 */
	private String mobile;

	/** 邮箱 */
	private String email;

	/** 头像 */
	private byte[] image;

	/** 公司ID */
	private String companyId;

	/** 部门ID */
	private String deptId;

	@Column(ignore = true)
	private String companyName;

	/** 部门名称 */
	@Column(ignore = true)
	private String deptName;

	/** it用户ID */
	private String itUserId;

	/** it用户姓名 */
	private String itUserName;

	/** 是否是领导1:是 0:否 */
	private Integer isLeader;

	/** 性别 0标示男 1标示女 2 */
	private Integer sex;

	/** 地址 */
	private String address;

	/** 传真 */
	private String fax;

	/** 失效月数 */
	private Integer failMonth;

	/** 失效时间 */
	@Column("failure_time")
	private Date failureTime;

	/** 权限时间戳 */
	private Integer aclTimestamp;

	/** 密码失效日期 */
	@Column("pwd_ftime")
	private Date pwdFtime;

	/** 初始密码是否已修改 1是0否 */
	private Integer pwdInit;

	/**
	 * @see com.phoenix.privilege.enums.UserTypeEnum 用户类型 0：自建用户 1：idm用户
	 */
	private Integer userType;
	/**  1是0否 */
	private Integer status;

}
