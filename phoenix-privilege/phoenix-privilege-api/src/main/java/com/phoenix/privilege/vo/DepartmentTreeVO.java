package com.phoenix.privilege.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentTreeVO {

	private String id;

	private String companyId;

	private String name;

	private String code;

	private String note;

	@JsonSerialize(using = ToStringSerializer.class)
	private Long pid;

	private String parentName;

	private Integer orderNo;

	private Integer leader;

	private Integer departmentType;

	private Integer status;

	private Integer nature;
	private Date createTime;
	private List<DepartmentTreeVO> children;

}
