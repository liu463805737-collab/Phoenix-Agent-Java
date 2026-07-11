package com.phoenix.privilege.entity;

import com.mybatisflex.annotation.Table;
import com.phoenix.privilege.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table("tbl_privilege_user_role")
public class PrivilegeUserRole extends BaseEntity {

	/** 用户ID */
	private String userId;

	/** 用户工号 */
	private String userNo;

	/** 角色ID */
	private String roleId;

	/** 截止日期 */
	private Date endDate;

	/** 有效期 */
	private Integer validMonth;

}
