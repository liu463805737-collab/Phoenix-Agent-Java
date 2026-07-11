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
public class PrivilegeRoleVO {

	private String id;

	private String name;

	private String sn;

	private String roleLevel;

	private String note;

	private Integer validState;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;

	private String creator;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;

	private String updator;

	private Integer delFlag;

	private Long companyId;

	private String companyName;

	private String deptName;

	private String systemId;

}
