package com.phoenix.privilege.dto.query;

import com.phoenix.common.model.PageQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 公司管理分页查询参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeCompanyQuery extends PageQuery {

	/** 公司名称 */
	private String cname;

	/** 公司简称 */
	private String shortName;

	/** 公司编码 */
	private String code;

}
