package com.phoenix.privilege.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.phoenix.privilege.dto.PrivilegeDepartmentDTO;
import com.phoenix.privilege.entity.PrivilegeCompany;
import com.phoenix.privilege.entity.PrivilegeDepartment;
import com.phoenix.privilege.service.IPrivilegeCompanyService;
import com.phoenix.privilege.service.IPrivilegeDepartmentService;
import com.phoenix.privilege.vo.DepartmentTreeVO;
import com.phoenix.privilege.vo.OrganizationTreeVO;
import com.phoenix.privilege.vo.PrivilegeDepartmentVO;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/privilege/department")
@RequiredArgsConstructor
public class PrivilegeDepartmentController {

	private final IPrivilegeDepartmentService privilegeDepartmentService;

	private final IPrivilegeCompanyService privilegeCompanyService;

	/**
	 * 返回组织树，最外层是公司，排序第一个的公司包含下级部门树
	 */
	@GetMapping("/orgTree")
	public ReturnVo<List<OrganizationTreeVO>> orgTree() {
		QueryWrapper qw = QueryWrapper.create().orderBy(PrivilegeCompany::getSort, true);
		List<PrivilegeCompany> companies = privilegeCompanyService.list(qw);
		List<OrganizationTreeVO> tree = new ArrayList<>();
		for (PrivilegeCompany company : companies) {
			OrganizationTreeVO node = OrganizationTreeVO.builder().id(company.getId()).name(company.getCname()).build();
			node.setChildren(privilegeDepartmentService.tree(company.getId()));
			tree.add(node);
		}
		return ReturnVo.ok(tree);
	}

	/**
	 * 分页查询部门列表
	 */
	@GetMapping("/page")
	public ReturnVo<Page<PrivilegeDepartmentVO>> page(@RequestParam(defaultValue = "1") long page,
			@RequestParam(defaultValue = "10") long size, PrivilegeDepartmentDTO dto) {
		QueryWrapper qw = QueryWrapper.create()
			.like(PrivilegeDepartment::getName, dto.getName(), StrUtil.isNotBlank(dto.getName()))
			.eq(PrivilegeDepartment::getCode, dto.getCode(), StrUtil.isNotBlank(dto.getCode()))
			.eq(PrivilegeDepartment::getCompanyId, dto.getCompanyId(), dto.getCompanyId() != null)
			.eq(PrivilegeDepartment::getPid, dto.getPid(), dto.getPid() != null)
			.orderBy(PrivilegeDepartment::getOrderNo, true);
		Page<PrivilegeDepartment> entityPage = privilegeDepartmentService.page(new Page<>(page, size), qw);
		Page<PrivilegeDepartmentVO> voPage = new Page<>(entityPage.getPageNumber(), entityPage.getPageSize(),
				entityPage.getTotalRow());
		voPage.setRecords(entityPage.getRecords()
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeDepartmentVO.class))
			.toList());
		return ReturnVo.ok(voPage);
	}

	/**
	 * 根据ID查询部门
	 */
	@GetMapping("/{id}")
	public ReturnVo<PrivilegeDepartmentVO> getById(@PathVariable Long id) {
		return ReturnVo
			.ok(BeanUtil.copyProperties(privilegeDepartmentService.getById(id), PrivilegeDepartmentVO.class));
	}

	/**
	 * 根据公司ID查询部门列表
	 */
	@GetMapping("/company/{companyId}")
	public ReturnVo<List<PrivilegeDepartmentVO>> getByCompanyId(@PathVariable String companyId) {
		List<PrivilegeDepartmentVO> list = privilegeDepartmentService.getByCompanyId(companyId)
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeDepartmentVO.class))
			.toList();
		return ReturnVo.ok(list);
	}

	/**
	 * 根据编码查询部门
	 */
	@GetMapping("/code/{code}")
	public ReturnVo<PrivilegeDepartmentVO> getByCode(@PathVariable String code) {
		return ReturnVo
			.ok(BeanUtil.copyProperties(privilegeDepartmentService.getByCode(code), PrivilegeDepartmentVO.class));
	}

	/**
	 * 获取部门树
	 */
	@GetMapping("/tree")
	public ReturnVo<List<DepartmentTreeVO>> tree(@RequestParam(required = false) String companyId) {
		return ReturnVo.ok(privilegeDepartmentService.tree(companyId));
	}

	/**
	 * 根据上级部门ID查询子部门
	 */
	@GetMapping("/pid/{pid}")
	public ReturnVo<List<PrivilegeDepartmentVO>> getByPid(@PathVariable String pid) {
		List<PrivilegeDepartmentVO> list = privilegeDepartmentService.getByPid(pid)
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeDepartmentVO.class))
			.toList();
		return ReturnVo.ok(list);
	}

	/**
	 * 新增部门
	 */
	@PostMapping
	public ReturnVo<Boolean> save(@RequestBody PrivilegeDepartmentDTO dto) {

		String loginId = (String) StpUtil.getLoginId();
		dto.setCreateBy(loginId);
		dto.setDepartmentType(0); // 默认自建部门
		return ReturnVo.ok(privilegeDepartmentService.save(dto.toEntity()));
	}

	/**
	 * 更新部门
	 */
	@PutMapping
	public ReturnVo<Boolean> update(@RequestBody PrivilegeDepartmentDTO dto) {
		String loginId = (String) StpUtil.getLoginId();
		PrivilegeDepartment entity = dto.toEntity();
		entity.setUpdateBy(loginId);
		entity.setUpdateTime(new Date());
		return ReturnVo.ok(privilegeDepartmentService.updateById(entity));
	}

	/**
	 * 删除部门
	 */
	@DeleteMapping("/{id}")
	public ReturnVo<Boolean> delete(@PathVariable String id) {
		return ReturnVo.ok(privilegeDepartmentService.deleteById(id));
	}

	/**
	 * 从三方平台同步部门（钉钉/飞书/企业微信）
	 */
	@PostMapping("/sync")
	public ReturnVo<Void> sync(@RequestParam String companyId) {
		privilegeDepartmentService.syncFromPlatform(companyId);
		return ReturnVo.ok();
	}

	/**
	 * 根据部门ID递归同步子部门（从三方平台）
	 */
	@PostMapping("/sync-children/{deptId}")
	public ReturnVo<Void> syncChildren(@PathVariable String deptId) {
		privilegeDepartmentService.syncChildrenFromPlatform(deptId);
		return ReturnVo.ok();
	}

}
