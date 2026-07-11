package com.phoenix.privilege.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.phoenix.privilege.dto.PasswordUpdateDTO;
import com.phoenix.privilege.dto.PrivilegeUserDTO;
import com.phoenix.privilege.entity.PrivilegeCompany;
import com.phoenix.privilege.entity.PrivilegeDepartment;
import com.phoenix.privilege.entity.PrivilegeUser;
import com.phoenix.privilege.enums.AuthErrorCode;
import com.phoenix.privilege.service.IPrivilegeCompanyService;
import com.phoenix.privilege.service.IPrivilegeDepartmentService;
import com.phoenix.privilege.service.IPrivilegeUserService;
import com.phoenix.privilege.vo.PrivilegeUserVO;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/privilege/user")
@RequiredArgsConstructor
public class PrivilegeUserController {

	private final IPrivilegeUserService privilegeUserService;
	private final IPrivilegeCompanyService privilegeCompanyService;
	private final IPrivilegeDepartmentService privilegeDepartmentService;

	/**
	 * 分页查询用户列表
	 */
	@GetMapping("/page")
	public ReturnVo<Page<PrivilegeUserVO>> page(@RequestParam(defaultValue = "1") long pageNumber,
			@RequestParam(defaultValue = "10") long pageSize, PrivilegeUserDTO dto) {
		return ReturnVo.ok(privilegeUserService.pageByQuery(new Page<>(pageNumber, pageSize), dto));
	}

	@GetMapping("/{id}")
	public ReturnVo<PrivilegeUserVO> getById(@PathVariable Long id) {
		return ReturnVo.ok(toVo(privilegeUserService.getById(id)));
	}

	@GetMapping("/username/{username}")
	public ReturnVo<PrivilegeUserVO> getByUsername(@PathVariable String username) {
		return ReturnVo.ok(toVo(privilegeUserService.getByUsername(username)));
	}

	@GetMapping("/code/{code}")
	public ReturnVo<PrivilegeUserVO> getByCode(@PathVariable String code) {
		return ReturnVo.ok(toVo(privilegeUserService.getByCode(code)));
	}

	private PrivilegeUserVO toVo(PrivilegeUser entity) {
		if (entity == null) {
			return null;
		}
		PrivilegeUserVO vo = BeanUtil.copyProperties(entity, PrivilegeUserVO.class);
		if (entity.getCompanyId() != null) {
			PrivilegeCompany company = privilegeCompanyService.getById(entity.getCompanyId());
			if (company != null) {
				vo.setCompanyName(company.getCname());
			}
		}
		if (entity.getDeptId() != null) {
			PrivilegeDepartment department = privilegeDepartmentService.getById(entity.getDeptId());
			if (department != null) {
				vo.setDeptName(department.getName());
			}
		}
		return vo;
	}

	/**
	 * 新增用户（校验用户名和手机号唯一性）
	 */
	@PostMapping
	public ReturnVo<Boolean> save(@RequestBody PrivilegeUserDTO dto) {
		if (privilegeUserService.checkUsernameExist(dto.getUsername())) {
			return ReturnVo.fail(AuthErrorCode.USERNAME_EXIST.getMessage(), AuthErrorCode.USERNAME_EXIST.getCode());
		}
		if (StrUtil.isNotBlank(dto.getMobile()) && privilegeUserService.checkMobileExist(dto.getMobile())) {
			return ReturnVo.fail(AuthErrorCode.MOBILE_EXIST.getMessage(), AuthErrorCode.MOBILE_EXIST.getCode());
		}
		return ReturnVo.ok(privilegeUserService.saveUser(dto));
	}

	/**
	 * 更新用户信息
	 */
	@PutMapping
	public ReturnVo<Boolean> update(@RequestBody PrivilegeUserDTO dto) {
		return ReturnVo.ok(privilegeUserService.updateById(dto.toEntity()));
	}

	/**
	 * 删除用户
	 */
	@DeleteMapping("/{id}")
	public ReturnVo<Boolean> delete(@PathVariable String id) {
		return ReturnVo.ok(privilegeUserService.deleteUser(id));
	}

	/**
	 * 修改密码（需校验原密码）
	 */
	@PutMapping("/password")
	public ReturnVo<Void> updatePassword(@RequestBody PasswordUpdateDTO dto) {
		if (!privilegeUserService.updatePassword(dto.getUserId(), dto.getOldPassword(), dto.getNewPassword())) {
			return ReturnVo.fail(AuthErrorCode.OLD_PASSWORD_ERROR.getMessage(),
					AuthErrorCode.OLD_PASSWORD_ERROR.getCode());
		}
		return ReturnVo.ok("密码修改成功");
	}
	@PutMapping("/setPassword")
	public ReturnVo<Void> setPassword(@RequestBody PasswordUpdateDTO dto) {
		if (!privilegeUserService.setPassword(dto.getUserId(), dto.getNewPassword())) {
			return ReturnVo.fail("设置密码失败！");
		}
		return ReturnVo.ok("密码修改成功");
	}

	/**
	 * 重置密码（返回随机生成的明文密码）
	 */
	@PutMapping("/reset-password/{id}")
	public ReturnVo<String> resetPassword(@PathVariable String id) {
		String newPassword = privilegeUserService.resetPassword(id);
		if (newPassword == null) {
			return ReturnVo.fail("用户不存在");
		}
		return ReturnVo.ok("重置成功", newPassword);
	}
}
