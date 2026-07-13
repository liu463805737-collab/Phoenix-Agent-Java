package com.phoenix.privilege.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.phoenix.privilege.dto.PrivilegeRoleDTO;
import com.phoenix.privilege.dto.query.PrivilegeRoleQuery;
import com.phoenix.privilege.service.IPrivilegeRoleService;
import com.phoenix.privilege.vo.PrivilegeRoleVO;
import com.phoenix.privilege.vo.RoleAclVO;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/privilege/role")
@RequiredArgsConstructor
public class PrivilegeRoleController {

	private final IPrivilegeRoleService privilegeRoleService;

	@PostMapping("/page")
	public ReturnVo<Page<PrivilegeRoleVO>> page(@RequestBody PrivilegeRoleQuery query) {
		return ReturnVo.ok(privilegeRoleService.pageByQuery(query));
	}

	@GetMapping("/{id}")
	public ReturnVo<PrivilegeRoleVO> getById(@PathVariable String id) {
		return ReturnVo.ok(BeanUtil.copyProperties(privilegeRoleService.getById(id), PrivilegeRoleVO.class));
	}

	@GetMapping("/company/{companyId}")
	public ReturnVo<List<PrivilegeRoleVO>> getByCompanyId(@PathVariable Long companyId) {
		List<PrivilegeRoleVO> list = privilegeRoleService.getByCompanyId(companyId)
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeRoleVO.class))
			.toList();
		return ReturnVo.ok(list);
	}

	@PostMapping
	public ReturnVo<Boolean> save(@RequestBody PrivilegeRoleDTO dto) {
		dto.setCreateBy((String) StpUtil.getLoginId());
		return ReturnVo.ok(privilegeRoleService.save(dto.toEntity()));
	}

	@PutMapping
	public ReturnVo<Boolean> update(@RequestBody PrivilegeRoleDTO dto) {
		return ReturnVo.ok(privilegeRoleService.updateById(dto.toEntity()));
	}

	@DeleteMapping("/{id}")
	public ReturnVo<Boolean> delete(@PathVariable String id) {
		privilegeRoleService.deleteRoleById(id);
		return ReturnVo.ok();
	}

	/**
	 * 获取当前角色下的权限制，与选中情况
	 * @param roleId
	 * @return
	 */
	@GetMapping("/{roleId}/acls")
	public ReturnVo<List<RoleAclVO>> getRoleAcls(@PathVariable String roleId) {
		return ReturnVo.ok(privilegeRoleService.getRoleAcls(roleId));
	}

}
