package com.phoenix.privilege.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.ChainQuery;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.privilege.dto.PrivilegeAclDTO;
import com.phoenix.privilege.entity.PrivilegeAcl;
import com.phoenix.privilege.entity.PrivilegeModule;
import com.phoenix.privilege.entity.PrivilegeUserRole;
import com.phoenix.privilege.mapper.PrivilegeAclMapper;
import com.phoenix.privilege.service.IPrivilegeAclService;
import com.phoenix.privilege.service.IPrivilegeModuleService;
import com.phoenix.privilege.service.IPrivilegeUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class PrivilegeAclServiceImpl extends ServiceImpl<PrivilegeAclMapper, PrivilegeAcl>
		implements IPrivilegeAclService {

	@Lazy
	@Autowired
	private IPrivilegeModuleService privilegeModuleService;

	@Lazy
	@Autowired
	private IPrivilegeUserRoleService privilegeUserRoleService;

	@Override
	public void deleteAclByReleaseId(String releaseId) {
		this.remove(QueryChain.of(this.mapper).eq(PrivilegeAcl::getReleaseId, releaseId));
	}

	@Override
	public List<PrivilegeAcl> getByUserId(String userId) {
		Map<String, PrivilegeAcl> aclMap = new HashMap<>();
		List<PrivilegeUserRole> userRoles = privilegeUserRoleService.getByUserId(userId);
		if (CollUtil.isNotEmpty(userRoles)) {
			List<String> roleIds = userRoles.stream().map(PrivilegeUserRole::getRoleId).toList();
			for (String roleId : roleIds) {
				List<PrivilegeAcl> privilegeAcls = this.getByReleaseId(roleId);
				for (PrivilegeAcl privilegeAcl : privilegeAcls) {
					if (aclMap.containsKey(privilegeAcl.getModuleId())) {
						privilegeAcl.setAclState(
								privilegeAcl.getAclState() | aclMap.get(privilegeAcl.getModuleId()).getAclState());
					}
					else {
						aclMap.put(privilegeAcl.getModuleId(), privilegeAcl);
					}
				}
			}
		}
		return new ArrayList<>(aclMap.values());
	}

	@Override
	public List<PrivilegeAcl> getByReleaseId(String releaseId) {
		return QueryChain.of(getMapper()).eq(PrivilegeAcl::getReleaseId, releaseId).list();
	}

	@Override
	public PrivilegeAcl getByReleaseIdAndModuleId(String releaseId, String moduleId) {
		return QueryChain.of(getMapper())
			.eq(PrivilegeAcl::getReleaseId, releaseId)
			.eq(PrivilegeAcl::getModuleId, moduleId)
			.one();
	}

	@Override
	public void saveAllAcl(String releaseId, Boolean checkStatus) {
		getMapper().deleteByReleaseId(releaseId);
		if (checkStatus) {
			List<PrivilegeModule> modules = privilegeModuleService.list();
			List<PrivilegeAcl> acls = new ArrayList<>();
			for (PrivilegeModule module : modules) {
				PrivilegeAcl privilegeAcl = new PrivilegeAcl();
				privilegeAcl.setReleaseId(releaseId);
				privilegeAcl.setModuleId(module.getId());
				privilegeAcl.setAclState(module.getState());
				privilegeAcl.setModuleSn(module.getSn());
				privilegeAcl.setReleaseSn("role");
				acls.add(privilegeAcl);
			}
			this.saveBatch(acls);
		}
	}

	@Override
	public void saveModuleAcl(PrivilegeAclDTO dto) {
		PrivilegeModule module = privilegeModuleService.getById(dto.getModuleId());
		PrivilegeAcl acl = getByReleaseIdAndModuleId(dto.getReleaseId(), module.getId());
		if (Objects.isNull(acl)) {
			PrivilegeAcl privilegeAcl = new PrivilegeAcl();
			privilegeAcl.setReleaseId(dto.getReleaseId());
			privilegeAcl.setModuleId(module.getId());
			privilegeAcl.setAclState(dto.getAclState());
			privilegeAcl.setModuleSn(module.getSn());
			privilegeAcl.setReleaseSn(dto.getReleaseSn());
			save(privilegeAcl);
		}
		else {
			acl.setAclState(dto.getAclState());
			this.updateById(acl);
		}

	}

	@Override
	public Boolean deleteAcl(String id) {
		return this.removeById(id);
	}

}
