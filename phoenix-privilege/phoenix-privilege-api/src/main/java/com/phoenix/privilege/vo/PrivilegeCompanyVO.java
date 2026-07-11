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
public class PrivilegeCompanyVO {

	private String id;

	private String cname;

	private String ename;

	private Long idmCompanyId;

	private String shortName;

	private String code;

	private String descr;

	private Integer status;

	private String creator;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;

	private String updator;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;

	private Integer delFlag;
	/** 序号 */
	private Integer sort;

}
