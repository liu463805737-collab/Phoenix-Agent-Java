package com.phoenix.privilege.entity;

import com.mybatisflex.annotation.Table;
import com.phoenix.privilege.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tbl_privilege_employee")
public class PrivilegeEmployee extends BaseEntity {


	/** 工号 */
	private String empCode;

	/** 员工姓名 */
	private String empName;

	/** 岗位编码 */
	private String positionCode;

	/** 职级编码 */
	private String jobGradeCode;

	/** 直属领导工号 */
	private String leaderUserId;

	/** 直属领导姓名 */
	private String leaderUserName;

	/** 公司id */
	private String companyId;

	/** 公司名称 */
	private String companyName;

	/** 部门id */
	private String deptId;

	/** 部门名称 */
	private String deptName;

	/** 0：保密 1：男 2：女 3：其他 */
	private Integer sex;

	/** 2离职3在职 */
	private Integer status;

	/** 是否禁用 */
	private Integer enableFlag;

	/** 入职时间 */
	private Date serviceDate;

	/** 离职时间 */
	private Date leaveDate;

	/** 三方平台union_id */
	private String thirdUnionId;

	/** 三方平台open_id */
	private String thirdOpenId;

	/** 三方平台user_id */
	private String thirdUserId;

	/** 手机号 */
	private String mobile;

	/** 邮箱 */
	private String email;

	/** 是否为部门负责人 */
	private String isDeptLeader;

	/** 全路径 */
	private String paths;

}
