package com.phoenix.privilege.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.common.enm.PlatformTypeEnm;
import com.phoenix.common.model.platform.PlatformInfo;
import com.phoenix.common.service.platform.DingTalkSdkService;
import com.phoenix.common.service.platform.FeishuSdkService;
import com.phoenix.common.service.platform.PlatformInfoService;
import com.phoenix.common.service.platform.WeixinSdkService;
import com.phoenix.common.exception.BusinessException;
import com.phoenix.privilege.entity.PrivilegeDepartment;
import com.phoenix.privilege.entity.PrivilegeEmployee;
import com.phoenix.privilege.entity.PrivilegeUser;
import com.phoenix.privilege.mapper.PrivilegeDepartmentMapper;
import com.phoenix.privilege.mapper.PrivilegeEmployeeMapper;
import com.phoenix.privilege.mapper.PrivilegeUserMapper;
import com.phoenix.privilege.service.IPrivilegeDepartmentService;
import com.phoenix.privilege.vo.DepartmentTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PrivilegeDepartmentServiceImpl extends ServiceImpl<PrivilegeDepartmentMapper, PrivilegeDepartment>
		implements IPrivilegeDepartmentService {

	private final PlatformInfoService platformInfoService;

	private final DingTalkSdkService dingTalkSdkService;

	private final FeishuSdkService feishuSdkService;

	private final WeixinSdkService weixinSdkService;

	private final PrivilegeUserMapper privilegeUserMapper;

	private final PrivilegeEmployeeMapper privilegeEmployeeMapper;

	@Override
	public List<PrivilegeDepartment> getByCompanyId(String companyId) {
		return QueryChain.of(getMapper())
			.eq(PrivilegeDepartment::getCompanyId, companyId)
			.orderBy(PrivilegeDepartment::getOrderNo, true)
			.list();
	}

	@Override
	public PrivilegeDepartment getByCode(String code) {
		return QueryChain.of(getMapper()).eq(PrivilegeDepartment::getCode, code).one();
	}

	@Override
	public List<PrivilegeDepartment> getByPid(String pid) {
		if (pid == null) {
			return QueryChain.of(getMapper())
				.isNull(PrivilegeDepartment::getPid)
				.orderBy(PrivilegeDepartment::getOrderNo, true)
				.list();
		}
		return QueryChain.of(getMapper())
			.eq(PrivilegeDepartment::getPid, pid)
			.orderBy(PrivilegeDepartment::getOrderNo, true)
			.list();
	}

	@Override
	public List<DepartmentTreeVO> tree(String companyId) {
		QueryWrapper qw = QueryWrapper.create()
			.eq(PrivilegeDepartment::getCompanyId, companyId, companyId != null)
			.orderBy(PrivilegeDepartment::getOrderNo, true);
		List<PrivilegeDepartment> list = list(qw);

		Map<String, String> idNameMap = list.stream()
			.collect(Collectors.toMap(PrivilegeDepartment::getId, PrivilegeDepartment::getName, (a, b) -> b));

		List<DepartmentTreeVO> nodeList = list.stream()
			.map(d -> BeanUtil.copyProperties(d, DepartmentTreeVO.class))
			.peek(n -> {
				if (n.getPid() != null) {
					n.setParentName(idNameMap.get(String.valueOf(n.getPid())));
				}
			})
			.toList();

		Map<Long, List<DepartmentTreeVO>> parentMap = nodeList.stream()
			.collect(Collectors.groupingBy(n -> n.getPid() != null ? n.getPid() : 0L, Collectors.toList()));

		List<DepartmentTreeVO> roots = new ArrayList<>();
		for (DepartmentTreeVO node : nodeList) {
			String nodeId = node.getId();
			List<DepartmentTreeVO> children = nodeId != null ? parentMap.getOrDefault(Long.valueOf(nodeId), List.of())
					: List.of();
			node.setChildren(children);
			if (node.getPid() == null) {
				roots.add(node);
			}
		}
		return roots;
	}

	@Override
	public void syncFromPlatform(String companyId) {
		PlatformInfo platform = platformInfoService.getEnabled();
		if (platform == null) {
			log.warn("未找到启用的三方平台配置，跳过同步");
			return;
		}
		PlatformTypeEnm platformType = PlatformTypeEnm.valueOfCode(platform.getType());
		if (platformType == null) {
			log.warn("未知的平台类型: {}", platform.getType());
			return;
		}
		try {
			List<JSONObject> depts = switch (platformType) {
				case DINGTALK -> dingTalkSdkService.getDepartments();
				case FEISHU -> feishuSdkService.getDepartments();
				case WEIXIN -> weixinSdkService.getDepartments();
			};
			syncDepartments(companyId, depts, platformType);
		}
		catch (Exception e) {
			log.error("同步{}部门失败", platformType.getDesc(), e);
		}
	}

	private void syncDepartments(String companyId, List<JSONObject> depts, PlatformTypeEnm platformType) {
		if (CollUtil.isEmpty(depts)) {
			return;
		}
		// code -> entity
		Map<String, PrivilegeDepartment> codeMap = new HashMap<>();
		for (JSONObject dept : depts) {
			// 跳过根部门（公司本身），公司已在tbl_privilege_company中维护
			if (isRootDept(dept, platformType)) {
				continue;
			}
			String code = getDeptCode(dept, platformType);
			String name = dept.getStr("name");
			Integer order = getDeptOrder(dept, platformType);
			if (StrUtil.isBlank(code)) {
				continue;
			}
			PrivilegeDepartment entity = getByCode(code);
			if (entity == null) {
				entity = new PrivilegeDepartment();
				entity.setCode(code);
			}
			entity.setCompanyId(companyId);
			entity.setName(name != null ? name : "");
			entity.setOrderNo(order != null ? order : 0);
			entity.setDepartmentType(1);
			saveOrUpdate(entity);
			codeMap.put(code, entity);
		}
		for (JSONObject dept : depts) {
			String code = getDeptCode(dept, platformType);
			String parentCode = getParentDeptCode(dept, platformType);
			if (StrUtil.isBlank(code) || StrUtil.isBlank(parentCode) || parentCode.equals(code)) {
				continue;
			}
			PrivilegeDepartment current = codeMap.get(code);
			PrivilegeDepartment parent = codeMap.get(parentCode);
			if (current != null && parent != null && parent.getId() != null) {
				current.setPid(parent.getId());
				updateById(current);
			}
		}
	}

	private static String getDeptCode(JSONObject dept, PlatformTypeEnm platformType) {
		return switch (platformType) {
			case DINGTALK -> dept.getStr("dept_id");
			case FEISHU -> dept.getStr("department_id");
			case WEIXIN -> dept.getStr("id");
		};
	}

	private static String getParentDeptCode(JSONObject dept, PlatformTypeEnm platformType) {
		return switch (platformType) {
			case DINGTALK -> {
				String pid = dept.getStr("parent_id");
				yield "0".equals(pid) || StrUtil.isBlank(pid) ? null : pid;
			}
			case FEISHU -> {
				String pid = dept.getStr("parent_department_id");
				yield "0".equals(pid) || StrUtil.isBlank(pid) ? null : pid;
			}
			case WEIXIN -> {
				String pid = dept.getStr("parentid");
				yield "0".equals(pid) || StrUtil.isBlank(pid) ? null : pid;
			}
		};
	}

	@Override
	public void syncChildrenFromPlatform(String deptId) {
		PrivilegeDepartment dept = getById(deptId);
		if (dept == null || StrUtil.isBlank(dept.getCode())) {
			log.warn("部门不存在或未绑定平台编码: {}", deptId);
			return;
		}
		PlatformInfo platform = platformInfoService.getEnabled();
		if (platform == null) {
			log.warn("未找到启用的三方平台配置，跳过同步");
			return;
		}
		PlatformTypeEnm platformType = PlatformTypeEnm.valueOfCode(platform.getType());
		if (platformType == null) {
			log.warn("未知的平台类型: {}", platform.getType());
			return;
		}
		List<JSONObject> allDepts;
		try {
			allDepts = switch (platformType) {
				case DINGTALK -> dingTalkSdkService.getDepartments();
				case FEISHU -> feishuSdkService.getDepartments();
				case WEIXIN -> weixinSdkService.getDepartments();
			};
		}
		catch (Exception e) {
			log.error("获取平台部门列表失败", e);
			return;
		}
		if (CollUtil.isEmpty(allDepts)) {
			return;
		}
		List<JSONObject> subtree = filterSubtree(allDepts, dept.getCode(), platformType);
		syncDepartments(dept.getCompanyId(), subtree, platformType);
	}

	private List<JSONObject> filterSubtree(List<JSONObject> allDepts, String rootCode, PlatformTypeEnm platformType) {
		Map<String, List<JSONObject>> parentMap = new HashMap<>();
		Map<String, JSONObject> codeMap = new HashMap<>();
		for (JSONObject dept : allDepts) {
			String code = getDeptCode(dept, platformType);
			String parentCode = getParentDeptCode(dept, platformType);
			if (StrUtil.isBlank(code)) {
				continue;
			}
			codeMap.put(code, dept);
			parentMap.computeIfAbsent(parentCode, k -> new ArrayList<>()).add(dept);
		}
		List<JSONObject> result = new ArrayList<>();
		Set<String> visited = new HashSet<>();
		Deque<String> stack = new ArrayDeque<>();
		stack.push(rootCode);
		while (!stack.isEmpty()) {
			String code = stack.pop();
			if (!visited.add(code)) {
				continue;
			}
			JSONObject node = codeMap.get(code);
			if (node != null) {
				result.add(node);
			}
			List<JSONObject> children = parentMap.get(code);
			if (CollUtil.isNotEmpty(children)) {
				for (JSONObject child : children) {
					String childCode = getDeptCode(child, platformType);
					if (childCode != null) {
						stack.push(childCode);
					}
				}
			}
		}
		return result;
	}

	@Override
	public boolean deleteById(String id) {
		long childCount = QueryChain.of(getMapper()).eq(PrivilegeDepartment::getPid, id).count();
		if (childCount > 0) {
			throw new BusinessException("该部门下存在下级部门，无法删除");
		}
		long userCount = QueryChain.of(privilegeUserMapper).eq(PrivilegeUser::getDeptId, id).count();
		if (userCount > 0) {
			throw new BusinessException("该部门下存在用户，无法删除");
		}
		long employeeCount = QueryChain.of(privilegeEmployeeMapper).eq(PrivilegeEmployee::getDeptId, id).count();
		if (employeeCount > 0) {
			throw new BusinessException("该部门下存在人员，无法删除");
		}
		return getMapper().deletePhysically(id) > 0;
	}

	private static Integer getDeptOrder(JSONObject dept, PlatformTypeEnm platformType) {
		return switch (platformType) {
			case DINGTALK -> dept.getInt("order");
			case FEISHU -> dept.getInt("order");
			case WEIXIN -> dept.getInt("order");
		};
	}

	private static boolean isRootDept(JSONObject dept, PlatformTypeEnm platformType) {
		return switch (platformType) {
			case DINGTALK -> "1".equals(dept.getStr("dept_id"));
			case FEISHU -> {
				String pid = dept.getStr("parent_department_id");
				yield StrUtil.isBlank(pid) || "0".equals(pid);
			}
			case WEIXIN -> "0".equals(dept.getStr("parentid"));
		};
	}

}
