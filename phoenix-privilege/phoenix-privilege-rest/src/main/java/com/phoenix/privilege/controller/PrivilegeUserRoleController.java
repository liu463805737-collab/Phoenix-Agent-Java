package com.phoenix.privilege.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.phoenix.privilege.dto.PrivilegeUserRoleDTO;
import com.phoenix.privilege.dto.UserRoleBatchDTO;
import com.phoenix.privilege.entity.PrivilegeUserRole;
import com.phoenix.privilege.service.IPrivilegeUserRoleService;
import com.phoenix.privilege.vo.PrivilegeUserRoleVO;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/privilege/user-role")
@RequiredArgsConstructor
public class PrivilegeUserRoleController {

	private final IPrivilegeUserRoleService privilegeUserRoleService;

	@GetMapping("/page")
	public ReturnVo<Page<PrivilegeUserRoleVO>> page(@RequestParam(defaultValue = "1") long page,
			@RequestParam(defaultValue = "10") long size, PrivilegeUserRoleDTO dto) {
		QueryWrapper qw = QueryWrapper.create()
			.eq(PrivilegeUserRole::getUserId, dto.getUserId(), StrUtil.isNotBlank(dto.getUserId()))
			.eq(PrivilegeUserRole::getRoleId, dto.getRoleId(), StrUtil.isNotBlank(dto.getRoleId()))
			.orderBy(PrivilegeUserRole::getCreateTime, false);
		Page<PrivilegeUserRole> entityPage = privilegeUserRoleService.page(new Page<>(page, size), qw);
		Page<PrivilegeUserRoleVO> voPage = new Page<>(entityPage.getPageNumber(), entityPage.getPageSize(),
				entityPage.getTotalRow());
		voPage.setRecords(entityPage.getRecords()
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeUserRoleVO.class))
			.toList());
		return ReturnVo.ok(voPage);
	}

	@GetMapping("/{id}")
	public ReturnVo<PrivilegeUserRoleVO> getById(@PathVariable String id) {
		return ReturnVo.ok(BeanUtil.copyProperties(privilegeUserRoleService.getById(id), PrivilegeUserRoleVO.class));
	}

	@GetMapping("/user/{userId}")
	public ReturnVo<List<PrivilegeUserRoleVO>> getByUserId(@PathVariable String userId) {
		List<PrivilegeUserRoleVO> list = privilegeUserRoleService.getByUserId(userId)
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeUserRoleVO.class))
			.toList();
		return ReturnVo.ok(list);
	}

	@GetMapping("/role/{roleId}")
	public ReturnVo<List<PrivilegeUserRoleVO>> getByRoleId(@PathVariable String roleId) {
		List<PrivilegeUserRoleVO> list = privilegeUserRoleService.getByRoleId(roleId)
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeUserRoleVO.class))
			.toList();
		return ReturnVo.ok(list);
	}

	@PostMapping
	public ReturnVo<Boolean> save(@RequestBody PrivilegeUserRoleDTO dto) {
		return ReturnVo.ok(privilegeUserRoleService.save(dto.toEntity()));
	}

	@PutMapping
	public ReturnVo<Boolean> update(@RequestBody PrivilegeUserRoleDTO dto) {
		return ReturnVo.ok(privilegeUserRoleService.updateById(dto.toEntity()));
	}

	@DeleteMapping("/{id}")
	public ReturnVo<Boolean> delete(@PathVariable String id) {
		return ReturnVo.ok(privilegeUserRoleService.removeById(id));
	}

	@PostMapping("/batch-save")
	public ReturnVo<Void> batchSave(@RequestBody UserRoleBatchDTO dto) {
		privilegeUserRoleService.batchSaveUsers(dto.getRoleId(), dto.getUserIds());
		return ReturnVo.ok();
	}

	@DeleteMapping("/batch-remove")
	public ReturnVo<Void> batchRemove(@RequestBody UserRoleBatchDTO dto) {
		privilegeUserRoleService.batchRemoveUsers(dto.getRoleId(), dto.getUserIds());
		return ReturnVo.ok();
	}

}
