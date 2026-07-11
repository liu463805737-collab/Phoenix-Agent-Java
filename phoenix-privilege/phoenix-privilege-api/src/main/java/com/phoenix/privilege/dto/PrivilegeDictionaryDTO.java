package com.phoenix.privilege.dto;

import com.phoenix.privilege.entity.PrivilegeDictionary;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeDictionaryDTO {

	public PrivilegeDictionary toEntity() {
		PrivilegeDictionary entity = new PrivilegeDictionary();
		BeanUtils.copyProperties(this, entity);
		return entity;
	}

	private Long id;

	private String code;

	private String name;

	private String pcode;

	private String systemSn;

	private String sn;

	private Integer orderNo;

}
