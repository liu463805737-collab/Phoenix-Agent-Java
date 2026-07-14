package com.phoenix.privilege.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.privilege.constant.LoginConstant;
import com.phoenix.privilege.dto.PrivilegeUserDTO;
import com.phoenix.privilege.entity.PrivilegeCompany;
import com.phoenix.privilege.entity.PrivilegeDepartment;
import com.phoenix.privilege.entity.PrivilegeRole;
import com.phoenix.privilege.entity.PrivilegeUser;
import com.phoenix.privilege.entity.PrivilegeUserRole;
import com.phoenix.privilege.mapper.PrivilegeUserMapper;
import com.phoenix.privilege.service.IPrivilegeRoleService;
import com.phoenix.privilege.service.IPrivilegeUserRoleService;
import com.phoenix.privilege.service.IPrivilegeUserService;
import com.phoenix.privilege.vo.PrivilegeRoleVO;
import com.phoenix.privilege.vo.PrivilegeUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PrivilegeUserServiceImpl extends ServiceImpl<PrivilegeUserMapper, PrivilegeUser>
		implements IPrivilegeUserService {

	private static final String RESET_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

	private final IPrivilegeRoleService privilegeRoleService;

	private final IPrivilegeUserRoleService privilegeUserRoleService;

	@Override
	public PrivilegeUser getByUsername(String username) {
		return QueryChain.of(getMapper()).eq(PrivilegeUser::getUsername, username).one();
	}

	@Override
	public PrivilegeUser getByCode(String code) {
		return QueryChain.of(getMapper()).eq(PrivilegeUser::getCode, code).one();
	}

	@Override
	public boolean checkUsernameExist(String username) {
		return QueryChain.of(getMapper()).eq(PrivilegeUser::getUsername, username).exists();
	}

	@Override
	public boolean checkMobileExist(String mobile) {
		return QueryChain.of(getMapper()).eq(PrivilegeUser::getMobile, mobile).exists();
	}

	@Override
	public boolean updatePassword(String userId, String oldPassword, String newPassword) {
		PrivilegeUser user = getById(userId);
		if (user == null) {
			return false;
		}
		String hashedOld = SecureUtil.md5(LoginConstant.PASSWORD_SALT + oldPassword);
		if (!hashedOld.equals(user.getPassword())) {
			return false;
		}
		user.setPassword(SecureUtil.md5(LoginConstant.PASSWORD_SALT + newPassword));
		user.setPwdInit(1);
		return updateById(user);
	}
	@Override
	public boolean setPassword(String userId, String newPassword) {
		PrivilegeUser user = getById(userId);
		if (user == null) {
			return false;
		}
		user.setPassword(SecureUtil.md5(LoginConstant.PASSWORD_SALT + newPassword));
		user.setPwdInit(1);
		return updateById(user);
	}

	@Override
	public String resetPassword(String userId) {
		PrivilegeUser user = getById(userId);
		if (user == null) {
			return null;
		}
		String plainPassword = generateRandomPassword(8);
		user.setPassword(SecureUtil.md5(LoginConstant.PASSWORD_SALT + plainPassword));
		user.setPwdInit(0);
		updateById(user);
		return plainPassword;
	}

	@Override
	public boolean saveUser(PrivilegeUserDTO dto) {
		PrivilegeUser entity = dto.toEntity();
		entity.setPassword(SecureUtil.md5(LoginConstant.PASSWORD_SALT + entity.getPassword()));
		entity.setStatus(0);
		entity.setPwdInit(0);
		boolean result = save(entity);
		if (result) {
			PrivilegeRole role = privilegeRoleService.list(QueryWrapper.create().eq(PrivilegeRole::getSn, "common"))
				.stream()
				.findFirst()
				.orElse(null);
			if (role != null) {
				PrivilegeUserRole userRole = PrivilegeUserRole.builder()
					.userId(entity.getId())
					.roleId(role.getId())
					.build();
				privilegeUserRoleService.save(userRole);
			}
		}
		return result;
	}

	@Override
	public boolean deleteUser(String id) {
		privilegeUserRoleService.removeUserRoleByUserId(id);
		return this.removeById(id);
	}

	@Override
	public Page<PrivilegeUserVO> pageByQuery(Page<PrivilegeUserVO> page, PrivilegeUserDTO dto) {
		QueryWrapper qw = QueryWrapper.create()
			.select("tbl_privilege_user.*, tbl_privilege_company.cname AS company_name, tbl_privilege_department.name AS dept_name")
			.leftJoin(PrivilegeCompany.class)
			.on(PrivilegeUser::getCompanyId, PrivilegeCompany::getId)
			.leftJoin(PrivilegeDepartment.class)
			.on(PrivilegeUser::getDeptId, PrivilegeDepartment::getId)
			.eq(PrivilegeUser::getCompanyId, dto.getCompanyId(), dto.getCompanyId() != null)
			.eq(PrivilegeUser::getDeptId, dto.getDeptId(), dto.getDeptId() != null)
			.eq(PrivilegeUser::getUserType, dto.getUserType(), dto.getUserType() != null);
		if (StrUtil.isNotBlank(dto.getKeyword())) {
			qw.and((Consumer<QueryWrapper>) w -> w.like(PrivilegeUser::getCode, dto.getKeyword())
				.or(PrivilegeUser::getUsername)
				.like(dto.getKeyword())
				.or(PrivilegeUser::getRealName)
				.like(dto.getKeyword())
				.or(PrivilegeUser::getPhone)
				.like(dto.getKeyword()));
		}
		qw.orderBy(PrivilegeUser::getCreateTime, false);
		Page<PrivilegeUser> entityPage = getMapper().paginate(page.getPageNumber(), page.getPageSize(), qw);
		Page<PrivilegeUserVO> voPage = new Page<>(entityPage.getPageNumber(), entityPage.getPageSize(),
				entityPage.getTotalRow());
		List<PrivilegeUser> records = entityPage.getRecords();
		if (records.isEmpty()) {
			voPage.setRecords(List.of());
			return voPage;
		}
		List<String> userIds = records.stream().map(PrivilegeUser::getId).toList();
		List<PrivilegeUserRole> userRoles = privilegeUserRoleService.list(
				QueryWrapper.create().in(PrivilegeUserRole::getUserId, userIds));
		List<String> roleIds = userRoles.stream().map(PrivilegeUserRole::getRoleId).distinct().toList();
		Map<String, String> roleNameMap = new HashMap<>();
		if (!roleIds.isEmpty()) {
			roleNameMap = privilegeRoleService.listByIds(roleIds).stream()
					.collect(HashMap::new, (m, r) -> m.put(r.getId(), r.getName()), HashMap::putAll);
		}
		Map<String, List<PrivilegeRoleVO>> userRolesMap = new HashMap<>();
		for (PrivilegeUserRole ur : userRoles) {
			userRolesMap.computeIfAbsent(ur.getUserId(), k -> new ArrayList<>())
					.add(PrivilegeRoleVO.builder().id(ur.getRoleId()).name(roleNameMap.getOrDefault(ur.getRoleId(), "")).build());
		}
		List<PrivilegeUserVO> voList = records.stream().map(e -> {
			PrivilegeUserVO vo = BeanUtil.copyProperties(e, PrivilegeUserVO.class);
			vo.setRoles(userRolesMap.getOrDefault(e.getId(), List.of()));
			return vo;
		}).toList();
		voPage.setRecords(voList);
		return voPage;
	}

	private String generateRandomPassword(int length) {
		Random random = new Random();
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(RESET_CHARS.charAt(random.nextInt(RESET_CHARS.length())));
		}
		return sb.toString();
	}

}
