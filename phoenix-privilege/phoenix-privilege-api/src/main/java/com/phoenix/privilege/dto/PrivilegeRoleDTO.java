package com.phoenix.privilege.dto;

import com.phoenix.privilege.entity.PrivilegeRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeRoleDTO {

	public PrivilegeRole toEntity() {
		PrivilegeRole entity = new PrivilegeRole();
		BeanUtils.copyProperties(this, entity);
		return entity;
	}

	private String id;

	private String name;

	private String sn;

	private String roleLevel;

	private String note;

	private Integer validState;

	private Long companyId;

	private String systemId;

	private String keyword;

	private String createBy;

}
