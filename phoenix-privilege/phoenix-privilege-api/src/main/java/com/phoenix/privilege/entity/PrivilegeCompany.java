package com.phoenix.privilege.entity;

import com.mybatisflex.annotation.Table;
import com.phoenix.privilege.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table("tbl_privilege_company")
public class PrivilegeCompany extends BaseEntity {

	/** 公司中文名称 */
	private String cname;

	/** 公司英文名称 */
	private String ename;

	/** idm公司id */
	private String idmCompanyId;

	/** 公司简称 */
	private String shortName;

	/** 公司code */
	private String code;

	/** 描述 */
	private String descr;

	/** 状态 1启用 0禁用 */
	private Integer status;
	/** 序号 */
	private Integer sort;

	/** 三方平台ID */
	private String thirdId;

}
