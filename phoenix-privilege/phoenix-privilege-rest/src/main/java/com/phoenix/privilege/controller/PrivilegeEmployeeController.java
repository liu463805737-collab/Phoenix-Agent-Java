package com.phoenix.privilege.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.phoenix.privilege.dto.PrivilegeEmployeeDTO;
import com.phoenix.privilege.service.IPrivilegeEmployeeService;
import com.phoenix.privilege.vo.PrivilegeEmployeeVO;
import com.phoenix.tools.vo.ReturnVo;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/privilege/employee")
@RequiredArgsConstructor
public class PrivilegeEmployeeController {

	private final IPrivilegeEmployeeService privilegeEmployeeService;

	/**
	 * 分页查询员工列表
	 */
	@GetMapping("/page")
	public ReturnVo<Page<PrivilegeEmployeeVO>> page(@RequestParam(defaultValue = "1") long page,
			@RequestParam(defaultValue = "10") long size, PrivilegeEmployeeDTO dto) {
		Page<PrivilegeEmployeeVO> entityPage = privilegeEmployeeService.page(new Page<>(page, size), dto);
		return ReturnVo.ok(entityPage);
	}

	/**
	 * 根据ID查询员工
	 */
	@GetMapping("/{id}")
	public ReturnVo<PrivilegeEmployeeVO> getById(@PathVariable String id) {
		return ReturnVo.ok(BeanUtil.copyProperties(privilegeEmployeeService.getById(id), PrivilegeEmployeeVO.class));
	}

	/**
	 * 根据工号查询员工
	 */
	@GetMapping("/emp-code/{empCode}")
	public ReturnVo<PrivilegeEmployeeVO> getByEmpCode(@PathVariable String empCode) {
		return ReturnVo.ok(BeanUtil.copyProperties(privilegeEmployeeService.getByEmpCode(empCode), PrivilegeEmployeeVO.class));
	}

	/**
	 * 新增员工
	 */
	@PostMapping
	public ReturnVo<Boolean> save(@RequestBody PrivilegeEmployeeDTO dto) {
		if(StringUtils.isBlank(dto.getDeptId())){
			return ReturnVo.error("人员部门不能为空！");
		}
		return ReturnVo.ok(privilegeEmployeeService.save(dto.toEntity()));
	}

	/**
	 * 更新员工
	 */
	@PutMapping
	public ReturnVo<Boolean> update(@RequestBody PrivilegeEmployeeDTO dto) {
		return ReturnVo.ok(privilegeEmployeeService.updateById(dto.toEntity()));
	}

	/**
	 * 删除员工
	 */
	@DeleteMapping("/{id}")
	public ReturnVo<Boolean> delete(@PathVariable String id) {
		return ReturnVo.ok(privilegeEmployeeService.removeById(id));
	}

	/**
	 * 从三方平台同步员工（钉钉/飞书/企业微信）
	 */
	@PostMapping("/sync")
	public ReturnVo<Void> sync(@RequestParam String companyId) {
		privilegeEmployeeService.syncFromPlatform(companyId);
		return ReturnVo.ok();
	}

	/**
	 * 根据部门ID递归同步该部门及所有子部门下的员工（从三方平台）
	 */
	@PostMapping("/sync-by-dept/{deptId}")
	public ReturnVo<Void> syncByDeptId(@PathVariable String deptId) {
		privilegeEmployeeService.syncByDeptId(deptId);
		return ReturnVo.ok();
	}

}
