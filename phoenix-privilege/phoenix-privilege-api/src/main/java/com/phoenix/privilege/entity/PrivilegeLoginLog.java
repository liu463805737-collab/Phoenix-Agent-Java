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
@Table("tbl_privilege_login_log")
public class PrivilegeLoginLog extends BaseEntity {

	/** 操作人id */
	private String operationId;

	/** 访问ip */
	private String ip;

	/** 操作人的姓名 */
	private String operationUsername;

	/** 操作人姓名 */
	private String operationPerson;

	/** 操作内容 */
	private String operationContent;

}
