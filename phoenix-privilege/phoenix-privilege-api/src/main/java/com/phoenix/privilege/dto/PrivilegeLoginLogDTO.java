package com.phoenix.privilege.dto;

import com.phoenix.privilege.entity.PrivilegeLoginLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeLoginLogDTO {

	public PrivilegeLoginLog toEntity() {
		PrivilegeLoginLog entity = new PrivilegeLoginLog();
		BeanUtils.copyProperties(this, entity);
		return entity;
	}

	private String id;

	private String operationId;

	private String ip;

	private String operationUsername;

	private String operationPerson;

	private String operationContent;

}
