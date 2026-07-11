package com.phoenix.privilege.entity;

import com.mybatisflex.annotation.Table;
import com.phoenix.privilege.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table("tbl_privilege_module")
public class PrivilegeModule extends BaseEntity {

	/** 名称 */
	private String name;

	/** 链接 */
	private String url;

	/** 标识 */
	private String sn;

	/** 存放该模块有哪些权限值可选 */
	private Integer state;

	private String component;

	/** 系统id */
	private Long systemId;

	private Integer status;

	/** 图片路径 */
	private String image;

	/** 模块的排序号 */
	private Integer orderNo;

	private Integer isShow;

	/** 父模块id */
	private String pid;

	/** 权限id */
	private Integer categoryId;

	/** 0目录，1菜单 */
	private String type;

}
