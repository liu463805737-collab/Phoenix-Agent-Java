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
@Table("tbl_privilege_pvalue")
public class PrivilegePvalue extends BaseEntity {

	/** 整型的位 */
	private Integer position;

	/** 名称 */
	private String name;

	/** 排序号 */
	private Integer orderNo;

	/** 备注 */
	private String remark;

}
