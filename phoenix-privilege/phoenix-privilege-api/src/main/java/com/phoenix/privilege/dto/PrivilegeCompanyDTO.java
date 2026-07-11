package com.phoenix.privilege.dto;

import com.phoenix.privilege.entity.PrivilegeCompany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeCompanyDTO {

	public PrivilegeCompany toEntity() {
		PrivilegeCompany entity = new PrivilegeCompany();
		BeanUtils.copyProperties(this, entity);
		return entity;
	}

	private String id;

	private String cname;

	private String ename;

	private String idmCompanyId;

	private String shortName;

	private String code;

	private String descr;

	private Integer status;

	private String createBy;
	/** 序号 */
	private Integer sort;
}
