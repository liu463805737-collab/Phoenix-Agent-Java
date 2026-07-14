package com.phoenix.privilege.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenix.privilege.constant.LoginConstant;
import com.phoenix.privilege.dto.LoginInfoDTO;
import com.phoenix.privilege.entity.*;
import com.phoenix.privilege.enums.AuthErrorCode;
import com.phoenix.privilege.service.*;
import com.phoenix.privilege.vo.LoginUserInfoVO;
import com.phoenix.privilege.vo.ModuleTreeVO;
import com.phoenix.privilege.vo.PrivilegePvalueVO;
import com.phoenix.privilege.vo.UserMenuVO;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.phoenix.privilege.constant.CommonConstant.LOGIN_ACLS;
import static com.phoenix.privilege.constant.CommonConstant.LOGIN_USER_INFO;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

	private final IPrivilegeUserService privilegeUserService;

	private final IPrivilegeLoginLogService privilegeLoginLogService;

	private final IPrivilegeUserRoleService privilegeUserRoleService;

	private final IPrivilegeModuleService privilegeModuleService;

	private final IPrivilegeAclService privilegeAclService;

	private final IPrivilegePvalueService privilegePvalueService;

	private final ObjectMapper objectMapper;

	@Override
	public ReturnVo<LoginUserInfoVO> login(LoginInfoDTO loginDTO, String ip) {
		/*
		 * CaptchaVerifyResult captchaResult =
		 * captchaService.verify(loginDTO.getCaptchaKey(), loginDTO.getCaptchaCode()); if
		 * (captchaResult == CaptchaVerifyResult.EXPIRED) { return
		 * ReturnVo.fail(AuthErrorCode.CAPTCHA_EXPIRED.getMessage(),
		 * AuthErrorCode.CAPTCHA_EXPIRED.getCode()); } if (captchaResult ==
		 * CaptchaVerifyResult.INVALID) { return
		 * ReturnVo.fail(AuthErrorCode.CAPTCHA_INVALID.getMessage(),
		 * AuthErrorCode.CAPTCHA_INVALID.getCode()); }
		 */
		if (loginDTO != null) {
			PrivilegeUser user = privilegeUserService.getByUsername(loginDTO.getUsername());
			if (user == null) {
				return ReturnVo.fail(AuthErrorCode.LOGIN_FAIL.getMessage(), AuthErrorCode.LOGIN_FAIL.getCode());
			}
			if (user.getStatus() == 1) {
				return ReturnVo.fail(AuthErrorCode.USER_DISABLED.getMessage(), AuthErrorCode.USER_DISABLED.getCode());
			}
			String hashedPassword = SecureUtil.md5(LoginConstant.PASSWORD_SALT + loginDTO.getPassword());
			if (!hashedPassword.equals(user.getPassword())) {
				return ReturnVo.fail(AuthErrorCode.PASSWORD_ERROR.getMessage(),
						AuthErrorCode.OLD_PASSWORD_ERROR.getCode());
			}
			StpUtil.login(user.getId());
			List<PrivilegeAcl> acls = privilegeAclService.getByUserId(user.getId());
			StpUtil.getSession().set(LOGIN_ACLS, acls);
			StpUtil.getSession().set(LOGIN_USER_INFO, user);
			String tokenValue = StpUtil.getTokenValue();
			LoginUserInfoVO loginVO = LoginUserInfoVO.builder()
				.token(tokenValue)
				.userId(user.getId())
				.username(user.getUsername())
				.realName(user.getRealName())
				.userType(user.getUserType())
				.build();
			PrivilegeLoginLog loginLog = PrivilegeLoginLog.builder()
				.operationId(user.getId())
				.operationUsername(user.getUsername())
				.operationPerson(user.getRealName())
				.ip(ip)
				.operationContent(LoginConstant.LOGIN_SUCCESS_OPERATION)
				.build();
			loginLog.setCreateTime(new Date());
			privilegeLoginLogService.save(loginLog);
			return ReturnVo.ok(loginVO);
		}
		return ReturnVo.fail("无效登录");
	}

	@Override
	public ReturnVo<Void> logout() {
		StpUtil.logout();
		return ReturnVo.ok(LoginConstant.LOGOUT_SUCCESS);
	}

	@Override
	@Transactional(readOnly = true)
	public ReturnVo<UserMenuVO> getUserMenus() {
		PrivilegeUser user = getPrivilegeUserFromSession();
		if (user == null) {
			return ReturnVo.fail("用户未登录");
		}
		List<PrivilegeAcl> acls = getAclsFromSession();
		List<PrivilegePvalue> allPvalues = privilegePvalueService.list();
		List<PrivilegePvalueVO> pvalueVOs = allPvalues.stream()
			.map(p -> BeanUtil.copyProperties(p, PrivilegePvalueVO.class))
			.toList();
		// TODO 开发阶段全部放开 生产的时候就采用acl的授权机制
		// List<ModuleTreeVO> modules =
		// privilegeModuleService.getModelTreeByUserId(user.getId(), acls);
		List<PrivilegeModule> modules = privilegeModuleService.list();
		List<ModuleTreeVO> menuTree = buildModuleTree(modules, buildAdminAclMap(modules, allPvalues));
		UserMenuVO vo = UserMenuVO.builder().menus(menuTree).pvalues(pvalueVOs).build();
		return ReturnVo.ok(vo);
	}

	private PrivilegeUser getPrivilegeUserFromSession() {
		Object value = StpUtil.getSession().get(LOGIN_USER_INFO);
		if (value instanceof PrivilegeUser pUser) {
			return pUser;
		}
		if (value instanceof Map<?, ?> map) {
			return objectMapper.convertValue(map, PrivilegeUser.class);
		}
		if (value instanceof String str) {
			try {
				return objectMapper.readValue(str, PrivilegeUser.class);
			}
			catch (Exception e) {
				log.warn("Failed to parse PrivilegeUser from session string", e);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private List<PrivilegeAcl> getAclsFromSession() {
		Object value = StpUtil.getSession().get(LOGIN_ACLS);
		if (value instanceof List<?> list) {
			return (List<PrivilegeAcl>) list;
		}
		if (value instanceof String str) {
			try {
				return objectMapper.readValue(str, List.class);
			}
			catch (Exception e) {
				log.warn("Failed to parse ACLs from session string", e);
			}
		}
		return List.of();
	}

	private Map<String, String> buildAdminAclMap(List<PrivilegeModule> modules, List<PrivilegePvalue> pvalues) {
		Map<String, String> map = new HashMap<>();
		for (PrivilegeModule module : modules) {
			int state = 0;
			for (PrivilegePvalue pv : pvalues) {
				state |= (1 << pv.getPosition());
			}
			map.put(module.getId(), String.valueOf(state));
		}
		return map;
	}

	private Map<String, String> buildUserAclMap(String userId) {
		List<PrivilegeUserRole> userRoles = privilegeUserRoleService.getByUserId(userId);
		Map<String, Integer> merged = new HashMap<>();
		for (PrivilegeUserRole ur : userRoles) {
			List<PrivilegeAcl> acls = privilegeAclService.queryChain()
				.eq(PrivilegeAcl::getReleaseId, ur.getRoleId())
				.eq(PrivilegeAcl::getReleaseSn, "role")
				.list();
			for (PrivilegeAcl acl : acls) {
				try {
					Integer state = acl.getAclState();
					merged.merge(acl.getModuleId(), state, (oldVal, newVal) -> oldVal | newVal);
				}
				catch (NumberFormatException ignored) {
				}
			}
		}
		Map<String, String> result = new HashMap<>();
		merged.forEach((k, v) -> result.put(k, String.valueOf(v)));
		return result;
	}

	private List<ModuleTreeVO> buildModuleTree(List<PrivilegeModule> allModules, Map<String, String> moduleAclMap) {
		List<ModuleTreeVO> nodeList = allModules.stream().map(m -> {
			ModuleTreeVO node = BeanUtil.copyProperties(m, ModuleTreeVO.class);
			String aclState = moduleAclMap.get(m.getId());
			if (aclState != null) {
				node.setState(Integer.parseInt(aclState));
			}
			return node;
		}).toList();

		Map<String, List<ModuleTreeVO>> parentMap = nodeList.stream()
			.collect(Collectors.groupingBy(n -> n.getPid() != null ? n.getPid() : "", Collectors.toList()));

		List<ModuleTreeVO> roots = new ArrayList<>();
		for (ModuleTreeVO node : nodeList) {
			List<ModuleTreeVO> children = parentMap.getOrDefault(node.getId(), List.of());
			node.setChildren(children);
			if (node.getPid() == null || node.getPid().isEmpty()) {
				roots.add(node);
			}
		}
		return roots;
	}

}
