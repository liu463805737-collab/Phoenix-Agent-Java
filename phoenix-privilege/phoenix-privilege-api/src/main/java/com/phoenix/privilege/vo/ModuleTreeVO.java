package com.phoenix.privilege.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleTreeVO {

	private String id;

	private String pid;

	private String name;

	private String url;

	private String sn;

	private Integer state;

	private String component;

	private Long systemId;

	private Integer status;

	private String image;

	private Integer orderNo;

	private Integer isShow;

	private Integer categoryId;

	private String type;

	private List<ModuleTreeVO> children;

	/** 该菜单对应的权限值列表（根据角色 ACL 计算） */
	private List<PvalueInfo> pvalues;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PvalueInfo {

		/** 权限值 ID */
		private String pvalueId;

		/** 权限值名称 */
		private String pvalueName;

		/** 位位置（对应 aclState 位掩码的偏移量） */
		private Integer position;

		/** 是否启用 */
		private Boolean enabled;
		/** 顺序 */
		private Integer orderNo;
	}

}
