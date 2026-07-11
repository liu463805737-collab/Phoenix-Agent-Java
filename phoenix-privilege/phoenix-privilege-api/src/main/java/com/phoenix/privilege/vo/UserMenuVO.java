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
public class UserMenuVO {

	private List<ModuleTreeVO> menus;

	private List<PrivilegePvalueVO> pvalues;

}
