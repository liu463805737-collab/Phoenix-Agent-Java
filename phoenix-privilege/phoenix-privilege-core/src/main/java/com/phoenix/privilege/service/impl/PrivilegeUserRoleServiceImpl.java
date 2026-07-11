package com.phoenix.privilege.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.privilege.entity.PrivilegeUserRole;
import com.phoenix.privilege.mapper.PrivilegeUserRoleMapper;
import com.phoenix.privilege.service.IPrivilegeUserRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class PrivilegeUserRoleServiceImpl extends ServiceImpl<PrivilegeUserRoleMapper, PrivilegeUserRole>
		implements IPrivilegeUserRoleService {
	@Override
	public void removeUserRoleByUserId(String userId) {
		QueryChain<PrivilegeUserRole> privilegeUserRoleQueryChain = QueryChain.of(this.mapper).eq(PrivilegeUserRole::getUserId, userId);
		remove(privilegeUserRoleQueryChain);
	}

	@Override
	public void removeUserRoleByRoleId(String roleId) {
		QueryChain<PrivilegeUserRole> privilegeUserRoleQueryChain = QueryChain.of(this.mapper).eq(PrivilegeUserRole::getRoleId, roleId);
		remove(privilegeUserRoleQueryChain);
	}

	@Override
	public List<PrivilegeUserRole> getByUserId(String userId) {
		return QueryChain.of(getMapper()).eq(PrivilegeUserRole::getUserId, userId).list();
	}

	@Override
	public List<PrivilegeUserRole> getByRoleId(String roleId) {
		return QueryChain.of(getMapper()).eq(PrivilegeUserRole::getRoleId, roleId).list();
	}

	@Override
	public void batchSaveUsers(String roleId, List<String> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return;
		}
		List<PrivilegeUserRole> list = new ArrayList<>(userIds.size());
		for (String userId : userIds) {
			PrivilegeUserRole entity = PrivilegeUserRole.builder().userId(userId).roleId(roleId).build();
			list.add(entity);
		}
		// 先删除，再新增
		batchRemoveUsers(roleId,userIds);
		saveBatch(list);

	}

	@Override
	public void batchRemoveUsers(String roleId, List<String> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return;
		}
		remove(QueryWrapper.create()
			.eq(PrivilegeUserRole::getRoleId, roleId)
			.in(PrivilegeUserRole::getUserId, userIds));
	}

}
