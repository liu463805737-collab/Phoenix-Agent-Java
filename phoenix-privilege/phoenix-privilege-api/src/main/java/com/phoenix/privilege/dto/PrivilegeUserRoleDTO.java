package com.phoenix.privilege.dto;

import com.phoenix.privilege.entity.PrivilegeUserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeUserRoleDTO {

	public PrivilegeUserRole toEntity() {
		PrivilegeUserRole entity = new PrivilegeUserRole();
		BeanUtils.copyProperties(this, entity);
		return entity;
	}

	private String id;

	private String userId;

	private String userNo;

	private String roleId;

	private Date endDate;

	private Integer validMonth;

}
