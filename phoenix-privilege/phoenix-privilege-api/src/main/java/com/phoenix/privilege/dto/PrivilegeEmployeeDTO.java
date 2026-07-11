package com.phoenix.privilege.dto;

import com.phoenix.privilege.entity.PrivilegeEmployee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeEmployeeDTO {

	public PrivilegeEmployee toEntity() {
		PrivilegeEmployee entity = new PrivilegeEmployee();
		BeanUtils.copyProperties(this, entity);
		return entity;
	}

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

	private Date serviceDate;

	private Date leaveDate;

	private String thirdUnionId;

	private String thirdOpenId;

	private String thirdUserId;

	private String mobile;

	private String email;

	private String isDeptLeader;

	private String paths;

	/** 关键字查询 */
	private String keyword;

}
