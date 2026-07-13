package com.phoenix.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncDeptDTO {

	private String sn;

	private String name;

	private String description;

	private Integer status;

	private String parentSn;

	private boolean root;

	/** 三方平台ID */
	private String thirdId;

}
