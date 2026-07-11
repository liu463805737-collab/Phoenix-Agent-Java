package com.phoenix.privilege.dto;

import com.phoenix.privilege.entity.PrivilegeModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeModuleDTO {

	public PrivilegeModule toEntity() {
		PrivilegeModule entity = new PrivilegeModule();
		BeanUtils.copyProperties(this, entity);
		return entity;
	}

	private String id;

	private String name;

	private String url;

	private String sn;

	private Integer state;

	private String component;

	private Long systemId;

	private Integer status;

	private String image;

	private Integer orderNo;

	private Integer isShow;

	private String pid;

	private Integer categoryId;

	private String type;

}
