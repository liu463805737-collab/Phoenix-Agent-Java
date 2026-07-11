package com.phoenix.privilege.dto;

import com.phoenix.privilege.entity.PrivilegeDepartment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeDepartmentDTO {

	public PrivilegeDepartment toEntity() {
		PrivilegeDepartment entity = new PrivilegeDepartment();
		BeanUtils.copyProperties(this, entity);
		return entity;
	}

	private String id;

	private String companyId;

	private String name;

	private String code;

	private String note;

	private String pid;

	private Integer orderNo;

	private Integer leader;

	private Integer departmentType;

	private Integer status;

	private Integer nature;

	private String createBy;
	private String updateBy;

}
