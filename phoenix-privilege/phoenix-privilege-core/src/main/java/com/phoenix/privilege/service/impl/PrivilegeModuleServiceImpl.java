package com.phoenix.privilege.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.privilege.entity.PrivilegeAcl;
import com.phoenix.privilege.entity.PrivilegeModule;
import com.phoenix.privilege.entity.PrivilegePvalue;
import com.phoenix.privilege.mapper.PrivilegeModuleMapper;
import com.phoenix.privilege.service.IPrivilegeAclService;
import com.phoenix.privilege.service.IPrivilegeModuleService;
import com.phoenix.privilege.service.IPrivilegePvalueService;
import com.phoenix.privilege.vo.ModuleTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PrivilegeModuleServiceImpl extends ServiceImpl<PrivilegeModuleMapper, PrivilegeModule>
		implements IPrivilegeModuleService {

	private final IPrivilegeAclService privilegeAclService;

	private final IPrivilegePvalueService privilegePvalueService;

	@Override
	public List<ModuleTreeVO> getModelTreeByUserId(String userId, List<PrivilegeAcl> acls) {
		if (CollUtil.isEmpty(acls)) {
			acls = privilegeAclService.getByUserId(userId);
		}
		List<String> moduleIds = new ArrayList<>();
		if (CollUtil.isNotEmpty(acls)) {
			acls.forEach(acl -> {
				if (acl.getPermission(3) > 0) {
					moduleIds.add(acl.getModuleId());
				}
			});
		}
		if (CollUtil.isNotEmpty(moduleIds)) {
			List<PrivilegeModule> modules = QueryChain.of(this.mapper)
				.in(PrivilegeModule::getId, moduleIds)
				.orderBy(PrivilegeModule::getOrderNo, true)
				.list();
			return BeanUtil.copyToList(modules, ModuleTreeVO.class);
		}
		return List.of();

	}

	@Override
	public List<PrivilegeModule> getBySystemId(Long systemId) {
		return QueryChain.of(getMapper())
			.eq(PrivilegeModule::getSystemId, systemId)
			.orderBy(PrivilegeModule::getOrderNo, true)
			.list();
	}

	@Override
	public List<PrivilegeModule> getByPid(String pid) {
		if (pid == null) {
			return QueryChain.of(getMapper())
				.isNull(PrivilegeModule::getPid)
				.orderBy(PrivilegeModule::getOrderNo, true)
				.list();
		}
		return QueryChain.of(getMapper())
			.eq(PrivilegeModule::getPid, pid)
			.orderBy(PrivilegeModule::getOrderNo, true)
			.list();
	}

	@Override
	public List<ModuleTreeVO> tree(Long systemId) {
		return tree(systemId, null);
	}

	@Override
	public List<ModuleTreeVO> tree(Long systemId, String releaseId) {
		// 1. 查询指定系统下的所有菜单模块，按排序号升序排列
		QueryWrapper qw = QueryWrapper.create()
			.eq(PrivilegeModule::getSystemId, systemId, systemId != null)
			.orderBy(PrivilegeModule::getOrderNo, true);
		List<PrivilegeModule> modules = list(qw);

		// 2. 查询所有权限值定义（如增、删、改、查等）
		List<PrivilegePvalue> allPvalues = privilegePvalueService.list();

		// 3. 如果传了 releaseId（角色/用户ID），则查询对应的 ACL 授权数据，按 moduleId 分组
		Map<String, PrivilegeAcl> aclMap;
		if (releaseId != null) {
			aclMap = privilegeAclService.queryChain()
				.eq(PrivilegeAcl::getReleaseId, releaseId)
				.list()
				.stream()
				.collect(Collectors.toMap(PrivilegeAcl::getModuleId, a -> a));
		}
		else {
			aclMap = Map.of();
		}

		// 4. 将菜单实体转为树节点 VO，并填充权限值列表
		List<ModuleTreeVO> nodeList = modules.stream().map(m -> {
			ModuleTreeVO node = BeanUtil.copyProperties(m, ModuleTreeVO.class);
			// 获取该菜单的 ACL 授权记录
			PrivilegeAcl acl = aclMap.get(m.getId());
			// aclStateInt：该角色对该菜单的实际授权状态（二进制位掩码）
			int aclStateInt = acl != null && acl.getAclState() != null ? acl.getAclState() : 0;
			if (acl != null && acl.getAclState() != null) {
				node.setState(acl.getAclState());
			}
			// moduleState：菜单自身定义的权限位掩码（十进制存储），标明该菜单有哪些权限位是有效的
			int moduleState = m.getState() != null ? m.getState() : 0;
			// 过滤并构建权限值列表：仅保留菜单自身 state 位掩码中定义的权限位
			List<ModuleTreeVO.PvalueInfo> pvalueInfos = allPvalues.stream()
				.filter(p -> (moduleState & (1 << p.getPosition())) != 0) // 只取菜单自身拥有的权限
				.map(p -> ModuleTreeVO.PvalueInfo.builder()
					.pvalueId(p.getId())
					.pvalueName(p.getName())
					.position(p.getPosition())
					.orderNo(p.getOrderNo())
					.enabled((aclStateInt & (1 << p.getPosition())) != 0) // 判断该权限是否已被授权
					.build())
				.sorted(Comparator.comparing(ModuleTreeVO.PvalueInfo::getOrderNo)) // 给有权限的按钮排序
				.toList();
			node.setPvalues(pvalueInfos);
			return node;
		}).toList();

		// 5. 构建树形结构：按父节点分组，将子节点挂载到对应的父节点下
		Map<String, List<ModuleTreeVO>> parentMap = nodeList.stream()
			.collect(Collectors.groupingBy(n -> n.getPid() != null ? n.getPid() : "", Collectors.toList()));

		List<ModuleTreeVO> roots = new ArrayList<>();
		for (ModuleTreeVO node : nodeList) {
			List<ModuleTreeVO> children = parentMap.getOrDefault(node.getId(), List.of());
			node.setChildren(children);
			// 根节点：没有父节点 ID 的节点
			if (node.getPid() == null || node.getPid().isEmpty()) {
				roots.add(node);
			}
		}
		return roots;
	}

}
