package com.phoenix.privilege.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleAclVO {

	/** 模块ID */
	private String moduleId;

	/** 模块名称 */
	private String moduleName;

	/** 模块标识 */
	private String moduleSn;

	/** ACL权限值（位掩码） */
	private Integer aclState;

	/** 该模块下所有权限值及启用状态 */
	private List<PvalueInfo> pvalues;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PvalueInfo {

		/** 权限值ID */
		private String pvalueId;

		/** 权限值名称 */
		private String pvalueName;

		/** 位位置（对应aclState位掩码的偏移量） */
		private Integer position;

		/** 是否启用 */
		private Boolean enabled;

	}

}
