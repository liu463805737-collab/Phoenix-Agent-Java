package com.phoenix.privilege.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.phoenix.privilege.dto.PrivilegeModuleDTO;
import com.phoenix.privilege.entity.PrivilegeModule;
import com.phoenix.privilege.entity.PrivilegePvalue;
import com.phoenix.privilege.service.IPrivilegeModuleService;
import com.phoenix.privilege.service.IPrivilegePvalueService;
import com.phoenix.privilege.vo.ModulePvalueVO;
import com.phoenix.privilege.vo.ModuleTreeVO;
import com.phoenix.privilege.vo.PrivilegeModuleVO;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/privilege/module")
@RequiredArgsConstructor
public class PrivilegeModuleController {

	private final IPrivilegeModuleService privilegeModuleService;
	private final IPrivilegePvalueService privilegePvalueService;

	/**
	 * 分页查询菜单列表
	 */
	@GetMapping("/page")
	public ReturnVo<Page<PrivilegeModuleVO>> page(@RequestParam(defaultValue = "1") long page,
			@RequestParam(defaultValue = "10") long size, PrivilegeModuleDTO dto) {
		QueryWrapper qw = QueryWrapper.create()
			.like(PrivilegeModule::getName, dto.getName(), StrUtil.isNotBlank(dto.getName()))
			.eq(PrivilegeModule::getSn, dto.getSn(), StrUtil.isNotBlank(dto.getSn()))
			.eq(PrivilegeModule::getSystemId, dto.getSystemId(), dto.getSystemId() != null)
			.eq(PrivilegeModule::getPid, dto.getPid(), StrUtil.isNotBlank(dto.getPid()))
			.orderBy(PrivilegeModule::getOrderNo, true);
		Page<PrivilegeModule> entityPage = privilegeModuleService.page(new Page<>(page, size), qw);
		Page<PrivilegeModuleVO> voPage = new Page<>(entityPage.getPageNumber(), entityPage.getPageSize(),
				entityPage.getTotalRow());
		voPage.setRecords(entityPage.getRecords()
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeModuleVO.class))
			.toList());
		return ReturnVo.ok(voPage);
	}

	/**
	 * 获取菜单树
	 */
	@GetMapping("/tree")
	public ReturnVo<List<ModuleTreeVO>> tree(@RequestParam(required = false) Long systemId) {
		return ReturnVo.ok(privilegeModuleService.tree(systemId));
	}

	/**
	 * 获取角色对应的菜单树（包含 ACL 权限值）
	 */
	@GetMapping("/tree/acl")
	public ReturnVo<List<ModuleTreeVO>> treeWithAcl(@RequestParam(required = false) Long systemId,
			@RequestParam String roleId) {
		return ReturnVo.ok(privilegeModuleService.tree(systemId, roleId));
	}

	/**
	 * 根据ID查询菜单
	 */
	@GetMapping("/{id}")
	public ReturnVo<PrivilegeModuleVO> getById(@PathVariable String id) {
		return ReturnVo.ok(BeanUtil.copyProperties(privilegeModuleService.getById(id), PrivilegeModuleVO.class));
	}

	/**
	 * 根据系统ID查询菜单列表
	 */
	@GetMapping("/system/{systemId}")
	public ReturnVo<List<PrivilegeModuleVO>> getBySystemId(@PathVariable Long systemId) {
		List<PrivilegeModuleVO> list = privilegeModuleService.getBySystemId(systemId)
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeModuleVO.class))
			.toList();
		return ReturnVo.ok(list);
	}

	/**
	 * 根据上级菜单ID查询子菜单
	 */
	@GetMapping("/pid/{pid}")
	public ReturnVo<List<PrivilegeModuleVO>> getByPid(@PathVariable String pid) {
		List<PrivilegeModuleVO> list = privilegeModuleService.getByPid(pid)
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeModuleVO.class))
			.toList();
		return ReturnVo.ok(list);
	}

	/**
	 * 新增菜单
	 */
	@PostMapping
	public ReturnVo<Boolean> save(@RequestBody PrivilegeModuleDTO dto) {
		return ReturnVo.ok(privilegeModuleService.save(dto.toEntity()));
	}

	/**
	 * 更新菜单
	 */
	@PutMapping
	public ReturnVo<Boolean> update(@RequestBody PrivilegeModuleDTO dto) {
		return ReturnVo.ok(privilegeModuleService.updateById(dto.toEntity()));
	}

	/**
	 * 获取模块的权限值列表（带 enabled 状态）
	 */
	@GetMapping("/{moduleId}/pvalues")
	public ReturnVo<List<ModulePvalueVO>> getModulePvalues(@PathVariable String moduleId) {
		PrivilegeModule module = privilegeModuleService.getById(moduleId);
		if (module == null) {
			return ReturnVo.fail("模块不存在");
		}
		int state = module.getState() != null ? module.getState() : 0;
		List<PrivilegePvalue> pvalues = privilegePvalueService.list();
		List<ModulePvalueVO> list = pvalues.stream()
			.map(p -> ModulePvalueVO.builder()
				.id(p.getId())
				.position(p.getPosition())
				.name(p.getName())
				.orderNo(p.getOrderNo())
				.remark(p.getRemark())
				.enabled((state & (1 << p.getPosition())) != 0)
				.build())
			.toList();
		return ReturnVo.ok(list);
	}

	/**
	 * 切换模块的单个权限值
	 */
	@PutMapping("/{moduleId}/pvalue/{position}/{enabled}")
	public ReturnVo<Boolean> updateModulePvalue(@PathVariable String moduleId,
			@PathVariable Integer position, @PathVariable Boolean enabled) {
		PrivilegeModule module = privilegeModuleService.getById(moduleId);
		if (module == null) {
			return ReturnVo.fail("模块不存在");
		}
		int state = module.getState() != null ? module.getState() : 0;
		if (enabled) {
			state |= (1 << position);
		} else {
			state &= ~(1 << position);
		}
		module.setState(state);
		return ReturnVo.ok(privilegeModuleService.updateById(module));
	}

	/**
	 * 删除菜单
	 */
	@DeleteMapping("/{id}")
	public ReturnVo<Boolean> delete(@PathVariable String id) {
		return ReturnVo.ok(privilegeModuleService.removeById(id));
	}

}
