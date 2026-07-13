package com.phoenix.privilege.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.privilege.dto.query.PrivilegeRoleQuery;
import com.phoenix.privilege.entity.PrivilegeAcl;
import com.phoenix.privilege.entity.PrivilegeModule;
import com.phoenix.privilege.entity.PrivilegePvalue;
import com.phoenix.privilege.entity.PrivilegeRole;
import com.phoenix.privilege.mapper.PrivilegeRoleMapper;
import com.phoenix.privilege.service.*;
import com.phoenix.privilege.vo.PrivilegeRoleVO;
import com.phoenix.privilege.vo.RoleAclVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PrivilegeRoleServiceImpl extends ServiceImpl<PrivilegeRoleMapper, PrivilegeRole>
		implements IPrivilegeRoleService {

	private final IPrivilegeAclService privilegeAclService;

	private final IPrivilegeModuleService privilegeModuleService;

	private final IPrivilegePvalueService privilegePvalueService;
	private final IPrivilegeUserRoleService privilegeUserRoleService;

	@Override
	public void deleteRoleById(String roleId) {
		privilegeUserRoleService.removeUserRoleByRoleId(roleId);
		privilegeAclService.deleteAclByReleaseId(roleId);
		removeById(roleId);
	}

	@Override
	public List<PrivilegeRole> getByCompanyId(Long companyId) {
		return QueryChain.of(getMapper())
			.eq(PrivilegeRole::getCompanyId, companyId)
			.orderBy(PrivilegeRole::getCreateTime, false)
			.list();
	}

	@Override
	public List<RoleAclVO> getRoleAcls(String roleId) {
		List<PrivilegeAcl> acls = privilegeAclService.queryChain()
			.eq(PrivilegeAcl::getReleaseId, roleId)
			.eq(PrivilegeAcl::getReleaseSn, "role")
			.list();

		if (acls.isEmpty()) {
			return List.of();
		}

		Map<String, PrivilegeModule> moduleMap = privilegeModuleService.list()
			.stream()
			.collect(Collectors.toMap(PrivilegeModule::getId, m -> m));

		List<PrivilegePvalue> allPvalues = privilegePvalueService.list();
		List<RoleAclVO> result = new ArrayList<>();

		for (PrivilegeAcl acl : acls) {
			PrivilegeModule module = moduleMap.get(acl.getModuleId());
			String moduleName = module != null ? module.getName() : null;

			Integer state;
			try {
				state = acl.getAclState();
			}
			catch (NumberFormatException e) {
				state = 0;
			}

			Integer finalState = state;
			List<RoleAclVO.PvalueInfo> pvalueInfos = allPvalues.stream()
				.map(p -> RoleAclVO.PvalueInfo.builder()
					.pvalueId(p.getId())
					.pvalueName(p.getName())
					.position(p.getPosition())
					.enabled((finalState.intValue() & (1 << p.getPosition())) != 0)
					.build())
				.toList();

			result.add(RoleAclVO.builder()
				.moduleId(acl.getModuleId())
				.moduleName(moduleName)
				.moduleSn(acl.getModuleSn())
				.aclState(acl.getAclState())
				.pvalues(pvalueInfos)
				.build());
		}

		return result;
	}

	@Override
	public Page<PrivilegeRoleVO> pageByQuery(PrivilegeRoleQuery query) {
		QueryWrapper qw = QueryWrapper.create()
			.select("tbl_privilege_role.*")
			.like(PrivilegeRole::getName, query.getName(), StrUtil.isNotBlank(query.getName()))
			.eq(PrivilegeRole::getSn, query.getSn(), StrUtil.isNotBlank(query.getSn()))
			.eq(PrivilegeRole::getCompanyId, query.getCompanyId(), query.getCompanyId() != null);
		qw.orderBy(PrivilegeRole::getCreateTime, false);
		Page<PrivilegeRole> entityPage = getMapper().paginate(query.getPage(), query.getSize(), qw);
		Page<PrivilegeRoleVO> voPage = new Page<>(entityPage.getPageNumber(), entityPage.getPageSize(),
				entityPage.getTotalRow());
		voPage.setRecords(
				entityPage.getRecords().stream().map(e -> BeanUtil.copyProperties(e, PrivilegeRoleVO.class)).toList());
		return voPage;
	}

}
