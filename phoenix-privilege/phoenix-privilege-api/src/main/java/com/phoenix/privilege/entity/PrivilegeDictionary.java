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
@Table("tbl_privilege_dictionary")
public class PrivilegeDictionary extends BaseEntity {

	/** 编码 */
	private String code;

	/** 名称 */
	private String name;

	/** 父编码 */
	private String pcode;

	/** 系统标识 */
	private String systemSn;

	/** 标识 */
	private String sn;

	/** 排序号 */
	private Integer orderNo;

}
