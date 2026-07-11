package com.phoenix.privilege.dto;

import com.phoenix.privilege.entity.PrivilegeAcl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeAclDTO {

	public PrivilegeAcl toEntity() {
		PrivilegeAcl entity = new PrivilegeAcl();
		BeanUtils.copyProperties(this, entity);
		return entity;
	}

	private String id;

	private String releaseId;

	private String releaseSn;

	private String systemSn;

	private String moduleId;

	private String moduleSn;

	private Integer aclState;

}
