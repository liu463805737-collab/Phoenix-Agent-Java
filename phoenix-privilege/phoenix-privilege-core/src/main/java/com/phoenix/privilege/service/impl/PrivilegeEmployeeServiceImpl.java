package com.phoenix.privilege.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.common.exception.BusinessException;
import com.phoenix.privilege.dto.PrivilegeEmployeeDTO;
import com.phoenix.common.enm.PlatformTypeEnm;
import com.phoenix.common.model.platform.PlatformInfo;
import com.phoenix.common.service.platform.DingTalkSdkService;
import com.phoenix.common.service.platform.FeishuSdkService;
import com.phoenix.common.service.platform.PlatformInfoService;
import com.phoenix.common.service.platform.WeixinSdkService;
import com.phoenix.privilege.entity.PrivilegeCompany;
import com.phoenix.privilege.entity.PrivilegeDepartment;
import com.phoenix.privilege.entity.PrivilegeEmployee;
import com.phoenix.privilege.mapper.PrivilegeDepartmentMapper;
import com.phoenix.privilege.mapper.PrivilegeEmployeeMapper;
import com.phoenix.privilege.service.IPrivilegeCompanyService;
import com.phoenix.privilege.service.IPrivilegeDepartmentService;
import com.phoenix.privilege.service.IPrivilegeEmployeeService;
import com.phoenix.privilege.vo.PrivilegeEmployeeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.phoenix.privilege.constant.CommonConstant.PARAM_NOT_NULL;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PrivilegeEmployeeServiceImpl extends ServiceImpl<PrivilegeEmployeeMapper, PrivilegeEmployee>
		implements IPrivilegeEmployeeService {

	private final PlatformInfoService platformInfoService;

	private final DingTalkSdkService dingTalkSdkService;

	private final FeishuSdkService feishuSdkService;

	private final WeixinSdkService weixinSdkService;

	private final PrivilegeDepartmentMapper privilegeDepartmentMapper;

	private final IPrivilegeCompanyService privilegeCompanyService;

	private final IPrivilegeDepartmentService privilegeDepartmentService;

	private final ThreadPoolTaskExecutorBuilder threadPoolTaskExecutorBuilder;

	@Override
	public Page<PrivilegeEmployeeVO> page(Page<PrivilegeEmployeeVO> page, PrivilegeEmployeeDTO dto) {
		QueryWrapper qw = QueryWrapper.create()
			.eq(PrivilegeEmployee::getDelFlag, 0)
			.like(PrivilegeEmployee::getEmpName, dto.getEmpName(), StrUtil.isNotBlank(dto.getEmpName()))
			.like(PrivilegeEmployee::getEmpCode, dto.getEmpCode(), StrUtil.isNotBlank(dto.getEmpCode()))
			.like(PrivilegeEmployee::getMobile, dto.getMobile(), StrUtil.isNotBlank(dto.getMobile()))
			.eq(PrivilegeEmployee::getCompanyId, dto.getCompanyId(), StrUtil.isNotBlank(dto.getCompanyId()))
			.eq(PrivilegeEmployee::getDeptId, dto.getDeptId(), StrUtil.isNotBlank(dto.getDeptId()))
			.eq(PrivilegeEmployee::getStatus, dto.getStatus(), dto.getStatus() != null)
			.eq(PrivilegeEmployee::getEnableFlag, dto.getEnableFlag(), dto.getEnableFlag() != null);
		if (StrUtil.isNotBlank(dto.getKeyword())) {
			qw.and((Consumer<QueryWrapper>) q -> q.like(PrivilegeEmployee::getEmpName, dto.getKeyword())
				.or(PrivilegeEmployee::getEmpCode)
				.like(dto.getKeyword())
				.or(PrivilegeEmployee::getMobile)
				.like(dto.getKeyword()));
		}
		qw.orderBy(PrivilegeEmployee::getCreateTime, false);
		Page<PrivilegeEmployee> entityPage = getMapper().paginate(page.getPageNumber(), page.getPageSize(), qw);
		Page<PrivilegeEmployeeVO> voPage = new Page<>(entityPage.getPageNumber(), entityPage.getPageSize(),
				entityPage.getTotalRow());
		voPage.setRecords(entityPage.getRecords()
			.stream()
			.map(e -> BeanUtil.copyProperties(e, PrivilegeEmployeeVO.class))
			.toList());
		return voPage;
	}

	@Override
	public PrivilegeEmployee getByEmpCode(String empCode) {
		return QueryChain.of(getMapper()).eq(PrivilegeEmployee::getEmpCode, empCode).one();
	}

	@Override
	public boolean save(PrivilegeEmployee entity) {
		PrivilegeDepartment department = privilegeDepartmentService.getById(entity.getDeptId());
		if (Objects.isNull(department)) {
			throw new BusinessException("部门" + PARAM_NOT_NULL);
		}
		entity.setDeptName(department.getName());
		PrivilegeCompany company = privilegeCompanyService.getById(department.getCompanyId());
		if (company != null) {
			entity.setCompanyId(company.getId());
			entity.setCompanyName(company.getCname());
		}

		return super.save(entity);
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
		// 构建部门 code → 内部ID 映射
		List<PrivilegeDepartment> depts = QueryChain.of(privilegeDepartmentMapper)
			.eq(PrivilegeDepartment::getCompanyId, companyId)
			.eq(PrivilegeDepartment::getDepartmentType, 1)
			.list();
		if (CollUtil.isEmpty(depts)) {
			log.warn("未找到已同步的部门，跳过员工同步");
			return;
		}
		Map<String, String> deptCodeIdMap = depts.stream()
			.filter(d -> StrUtil.isNotBlank(d.getCode()) && StrUtil.isNotBlank(d.getId()))
			.collect(Collectors.toMap(PrivilegeDepartment::getCode, PrivilegeDepartment::getId));

		for (PrivilegeDepartment dept : depts) {
			if (StrUtil.isBlank(dept.getCode())) {
				continue;
			}
			try {
				List<JSONObject> users = switch (platformType) {
					case DINGTALK -> dingTalkSdkService.getUsersByDepartmentId(dept.getCode());
					case FEISHU -> feishuSdkService.getUsersByDepartmentId(dept.getCode());
					case WEIXIN -> weixinSdkService.getUsersByDepartmentId(dept.getCode());
				};
				syncEmployees(companyId, users, platformType, deptCodeIdMap);
			}
			catch (Exception e) {
				log.error("同步部门 {} 人员失败", dept.getCode(), e);
			}
		}
	}

	@Override
	public void syncByDeptId(String deptId) {
		PrivilegeDepartment dept = QueryChain.of(privilegeDepartmentMapper)
			.eq(PrivilegeDepartment::getId, String.valueOf(deptId))
			.one();
		if (dept == null) {
			log.warn("部门不存在: {}", deptId);
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

		List<String> allDeptIds = new ArrayList<>();
		collectChildDeptIds(deptId, allDeptIds);
		allDeptIds.add(dept.getId());

		List<PrivilegeDepartment> departments = QueryChain.of(privilegeDepartmentMapper)
			.in(PrivilegeDepartment::getId, allDeptIds.toArray())
			.list();
		if (CollUtil.isEmpty(departments)) {
			return;
		}

		Map<String, String> deptCodeIdMap = departments.stream()
			.filter(d -> StrUtil.isNotBlank(d.getCode()) && StrUtil.isNotBlank(d.getId()))
			.collect(Collectors.toMap(PrivilegeDepartment::getCode, PrivilegeDepartment::getId));

		String companyId = dept.getCompanyId();
		for (PrivilegeDepartment d : departments) {
			if (StrUtil.isBlank(d.getCode())) {
				continue;
			}
			try {
				List<JSONObject> users = switch (platformType) {
					case DINGTALK -> dingTalkSdkService.getUsersByDepartmentId(d.getCode());
					case FEISHU -> feishuSdkService.getUsersByDepartmentId(d.getCode());
					case WEIXIN -> weixinSdkService.getUsersByDepartmentId(d.getCode());
				};
				syncEmployees(companyId, users, platformType, deptCodeIdMap);
			}
			catch (Exception e) {
				log.error("同步部门 {} 人员失败", d.getCode(), e);
			}
		}
	}

	private void collectChildDeptIds(String parentId, List<String> result) {
		List<PrivilegeDepartment> children = QueryChain.of(privilegeDepartmentMapper)
			.eq(PrivilegeDepartment::getPid, parentId)
			.list();
		for (PrivilegeDepartment child : children) {
			if (StrUtil.isNotBlank(child.getId())) {
				result.add(child.getId());
				collectChildDeptIds(child.getId(), result);
			}
		}
	}

	private void syncEmployees(String companyId, List<JSONObject> users, PlatformTypeEnm platformType,
			Map<String, String> deptCodeIdMap) {
		if (CollUtil.isEmpty(users)) {
			return;
		}
		for (JSONObject user : users) {
			String userId = getUserThirdUserId(user, platformType);
			if (StrUtil.isBlank(userId)) {
				continue;
			}
			PrivilegeEmployee emp = getByEmpCode(userId);
			if (emp == null) {
				emp = new PrivilegeEmployee();
				emp.setEmpCode(userId);
			}
			emp.setThirdUserId(userId);
			emp.setEmpName(user.getStr("name"));
			emp.setMobile(user.getStr("mobile"));
			emp.setEmail(user.getStr("email"));
			emp.setCompanyId(String.valueOf(companyId));
			emp.setStatus(3);
			emp.setEnableFlag(1);

			String deptCode = getFirstDeptCode(user, platformType);
			if (deptCode != null && deptCodeIdMap.containsKey(deptCode)) {
				emp.setDeptId(deptCodeIdMap.get(deptCode));
			}

			setThirdPartyIds(emp, user, platformType);
			saveOrUpdate(emp);
		}
	}

	private static String getUserThirdUserId(JSONObject user, PlatformTypeEnm platformType) {
		return switch (platformType) {
			case DINGTALK -> user.getStr("userid");
			case FEISHU -> user.getStr("user_id");
			case WEIXIN -> user.getStr("userid");
		};
	}

	private static String getFirstDeptCode(JSONObject user, PlatformTypeEnm platformType) {
		return switch (platformType) {
			case DINGTALK -> {
				List<Long> deptIds = user.getJSONArray("dept_id_list").toList(Long.class);
				yield CollUtil.isNotEmpty(deptIds) ? String.valueOf(deptIds.get(0)) : null;
			}
			case FEISHU -> {
				List<String> deptIds = user.getJSONArray("departments").toList(String.class);
				yield CollUtil.isNotEmpty(deptIds) ? deptIds.get(0) : null;
			}
			case WEIXIN -> {
				List<Integer> deptIds = user.getJSONArray("department").toList(Integer.class);
				yield CollUtil.isNotEmpty(deptIds) ? String.valueOf(deptIds.get(0)) : null;
			}
		};
	}

	private static void setThirdPartyIds(PrivilegeEmployee emp, JSONObject user, PlatformTypeEnm platformType) {
		switch (platformType) {
			case FEISHU -> {
				emp.setThirdUnionId(user.getStr("union_id"));
				emp.setThirdOpenId(user.getStr("open_id"));
			}
			default -> {
				emp.setThirdOpenId(user.getStr("userid"));
			}
		}
	}

}
