package com.phoenix.privilege.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeModuleVO {

	private String id;

	private String name;

	private String url;

	private String sn;

	private String state;

	private String component;

	private Long systemId;

	private Integer status;

	private String image;

	private Integer orderNo;

	private Integer isShow;

	private String pid;

	private Integer categoryId;

	private String type;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createdTime;

	private String createdBy;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updatedTime;

	private String updatedBy;

	private Integer delFlag;

}
