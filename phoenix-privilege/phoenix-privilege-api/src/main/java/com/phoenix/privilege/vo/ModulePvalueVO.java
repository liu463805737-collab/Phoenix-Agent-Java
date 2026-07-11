package com.phoenix.privilege.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModulePvalueVO {

	private String id;

	private Integer position;

	private String name;

	private Integer orderNo;

	private String remark;

	private boolean enabled;

}
