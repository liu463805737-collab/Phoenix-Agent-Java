package com.phoenix.privilege.entity;

import com.mybatisflex.annotation.Column;
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
@Table("tbl_privilege_role")
public class PrivilegeRole extends BaseEntity {

	/** 名称 */
	private String name;

	/** 标识 */
	private String sn;

	/** 角色等级【数据字典获取】 */
	private String roleLevel;

	/** 备注 */
	private String note;

	/** 有效状态（1：有效；0：失效） */
	private Integer validState;

	/** 公司id */
	private Long companyId;

	/** 系统id */
	private String systemId;

	@Column(ignore = true)
	private String companyName;

	@Column(ignore = true)
	private String deptName;

}
