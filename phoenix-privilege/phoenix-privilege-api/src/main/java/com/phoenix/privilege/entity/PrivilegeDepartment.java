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
@Table("tbl_privilege_department")
public class PrivilegeDepartment extends BaseEntity {

	/** 公司id */
	private String companyId;

	/** 名称 */
	private String name;

	/** 编号 */
	private String code;

	/** 备注 */
	private String note;

	/** 父id */
	private String pid;

	/** 排序号 */
	private Integer orderNo;

	/** 是否是leader1:是 0：不是 */
	private Integer leader;

	/** 是否为自建部门 0-自建部门，1-idm同步部门 */
	private Integer departmentType;

	/** 部门状态，0启用、1禁用 */
	private Integer status;

	/** 部门性质：0部门、1组 */
	private Integer nature;

}
