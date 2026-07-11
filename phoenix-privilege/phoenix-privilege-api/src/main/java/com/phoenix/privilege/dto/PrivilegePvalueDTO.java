package com.phoenix.privilege.dto;

import com.phoenix.privilege.entity.PrivilegePvalue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegePvalueDTO {

	public PrivilegePvalue toEntity() {
		PrivilegePvalue entity = new PrivilegePvalue();
		BeanUtils.copyProperties(this, entity);
		return entity;
	}

	private String id;

	private Integer position;

	private String name;

	private Integer orderNo;

	private String remark;

}
