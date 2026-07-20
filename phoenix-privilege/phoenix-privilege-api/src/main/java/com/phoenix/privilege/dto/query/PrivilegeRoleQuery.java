package com.phoenix.privilege.dto.query;

import com.phoenix.common.model.PageQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeRoleQuery extends PageQuery {

	private String name;

	private String sn;

	private Long companyId;

}
