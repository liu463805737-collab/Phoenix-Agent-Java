package com.phoenix.privilege.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeEmployeeVO {

	private String id;

	private String empCode;

	private String empName;

	private String positionCode;

	private String jobGradeCode;

	private String leaderUserId;

	private String leaderUserName;

	private String companyId;

	private String companyName;

	private String deptId;

	private String deptName;

	private Integer sex;

	private Integer status;

	private Integer enableFlag;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date serviceDate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date leaveDate;

	private String thirdUnionId;

	private String thirdOpenId;

	private String thirdUserId;

	private Integer delFlag;

	private String createBy;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;

	private String updateBy;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;

	private String mobile;

	private String email;

	private String isDeptLeader;

	private String paths;

}
