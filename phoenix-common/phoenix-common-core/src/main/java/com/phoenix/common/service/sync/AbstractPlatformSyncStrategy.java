package com.phoenix.common.service.sync;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import com.phoenix.common.model.dto.SyncDeptDTO;
import com.phoenix.common.model.dto.SyncUserDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractPlatformSyncStrategy implements PlatformSyncStrategy {

	/** 构造方法 */
	protected AbstractPlatformSyncStrategy() {
	}

	/** 全量同步：同步所有部门及人员 */
	@Override
	public String syncAll() {
		syncDepartments();
		syncUsers();
		return "全量同步完成";
	}

	/** 同步所有部门数据到本地权限系统 */
	@Override
	public String syncDepartments() {
		log.info("[{}] 开始同步部门", getPlatformName());
		List<JSONObject> depts = fetchDepartments();
		syncDeptToPrivilege(depts);
		log.info("[{}] 部门同步完成", getPlatformName());
		return "部门同步完成";
	}

	/** 将第三方部门列表同步到本地权限表（公司 + 部门），按树形结构逐层处理 */
	private void syncDeptToPrivilege(List<JSONObject> depts) {
		syncDeptToPrivilege(depts, null);
	}

	private void syncDeptToPrivilege(List<JSONObject> depts, String fallbackCompanyId) {
		List<SyncDeptDTO> dtos = depts.stream()
			.map(this::convertToDept)
			.filter(d -> StrUtil.isNotBlank(d.getSn()))
			.collect(Collectors.toList());

		Map<String, String> parentMap = new HashMap<>();
		for (SyncDeptDTO dto : dtos) {
			if (StrUtil.isNotBlank(dto.getParentSn())) {
				parentMap.put(dto.getSn(), dto.getParentSn());
			}
		}

		// 转成树型结构
		Map<String, String> companyIdMap = new HashMap<>();
		for (SyncDeptDTO dto : dtos) {
			if (dto.isRoot()) {
				String companyId = upsertCompany(dto);
				companyIdMap.put(dto.getSn(), companyId);
			}
		}
		for (SyncDeptDTO dto : dtos) {
			if (dto.isRoot()) {
				continue;
			}
			String companyId = resolveCompanyId(dto.getSn(), parentMap, companyIdMap);
			if (companyId == null) {
				companyId = fallbackCompanyId;
			}
			String parentLocalId = resolveLocalDeptId(dto.getParentSn());
			upsertDepartment(dto, companyId, parentLocalId);
		}
	}

	/** 根据部门SN向上递归查找所属公司ID，先从缓存取，再递归父级，最后查DB */
	private String resolveCompanyId(String deptSn, Map<String, String> parentMap, Map<String, String> companyIdMap) {
		if (companyIdMap.containsKey(deptSn)) {
			return companyIdMap.get(deptSn);
		}
		String current = deptSn;
		while (StrUtil.isNotBlank(current) && parentMap.containsKey(current)) {
			current = parentMap.get(current);
			if (companyIdMap.containsKey(current)) {
				return companyIdMap.get(current);
			}
		}
		return queryCompanyIdFromDb(deptSn);
	}

	/** 根据三方部门ID从数据库查询所属公司ID，先查部门表(按third_id)再查公司表(按third_id) */
	private String queryCompanyIdFromDb(String deptThirdId) {
		if (StrUtil.isBlank(deptThirdId))
			return null;
		Row row = Db.selectOneBySql(
				"SELECT company_id FROM tbl_privilege_department WHERE third_id = ? AND del_flag = 0 AND company_id IS NOT NULL",
				deptThirdId);
		if (row != null)
			return (String) row.get("company_id");
		row = Db.selectOneBySql("SELECT id FROM tbl_privilege_company WHERE third_id = ? AND del_flag = 0 LIMIT 1",
				deptThirdId);
		return row != null ? (String) row.get("id") : null;
	}

	/** 新增或更新公司记录（按cname + third_id判断是否存在） */
	private String upsertCompany(SyncDeptDTO dto) {
		String existId = queryPrivilegeCompanyId(dto.getName(), dto.getSn());
		if (existId != null) {
			Db.updateBySql("UPDATE tbl_privilege_company SET cname = ?, third_id = ?, status = 1 WHERE id = ?",
					dto.getName(), dto.getSn(), existId);
			return existId;
		}
		String id = IdUtil.getSnowflakeNextIdStr();
		Db.insertBySql(
				"INSERT INTO tbl_privilege_company (id, cname, third_id, status, sort, del_flag) VALUES (?, ?, ?, 1, 0, 0)",
				id, dto.getName(), dto.getSn());
		return id;
	}

	/** 新增或更新部门记录（按third_id判断是否存在） */
	private String upsertDepartment(SyncDeptDTO dto, String companyId, String parentLocalId) {
		String existId = queryPrivilegeDeptId(dto.getSn());
		if (existId != null) {
			Db.updateBySql(
					"UPDATE tbl_privilege_department SET name = ?, company_id = ?, pid = ?, third_id = ?, department_type = 1, status = 0 WHERE id = ?",
					dto.getName(), companyId, parentLocalId, dto.getSn(), existId);
			return existId;
		}
		String id = IdUtil.getSnowflakeNextIdStr();
		Db.insertBySql(
				"INSERT INTO tbl_privilege_department (id, company_id, name, pid, third_id, department_type, status, del_flag) VALUES (?, ?, ?, ?, ?, 1, 0, 0)",
				id, companyId, dto.getName(), parentLocalId, dto.getSn());
		return id;
	}

	/** 根据第三方部门ID查询本地部门主键ID */
	private String resolveLocalDeptId(String platformDeptId) {
		if (StrUtil.isBlank(platformDeptId)) {
			return null;
		}
		return queryPrivilegeDeptId(platformDeptId);
	}

	/** 根据公司名称 + 三方ID从数据库查询公司ID */
	private String queryPrivilegeCompanyId(String name, String thirdId) {
		Row row = Db.selectOneBySql(
				"SELECT id FROM tbl_privilege_company WHERE cname = ? AND third_id = ? AND del_flag = 0",
				name, thirdId);
		return row != null ? (String) row.get("id") : null;
	}

	/** 根据三方ID从数据库查询部门ID */
	private String queryPrivilegeDeptId(String thirdId) {
		Row row = Db.selectOneBySql("SELECT id FROM tbl_privilege_department WHERE third_id = ? AND del_flag = 0",
				thirdId);
		return row != null ? (String) row.get("id") : null;
	}

	/** 根据本地部门ID查询第三方部门ID */
	private String queryThirdIdByLocalDeptId(String localDeptId) {
		if (StrUtil.isBlank(localDeptId))
			return null;
		Row row = Db.selectOneBySql("SELECT third_id FROM tbl_privilege_department WHERE id = ? AND del_flag = 0",
				localDeptId);
		return row != null ? (String) row.get("third_id") : null;
	}

	/** 根据本地部门ID查询所属公司ID */
	private String queryCompanyIdByLocalDeptId(String localDeptId) {
		if (StrUtil.isBlank(localDeptId))
			return null;
		Row row = Db.selectOneBySql("SELECT company_id FROM tbl_privilege_department WHERE id = ? AND del_flag = 0",
				localDeptId);
		return row != null ? (String) row.get("company_id") : null;
	}

	/** 同步所有部门下的人员数据 */
	@Override
	public String syncUsers() {
		log.info("[{}] 开始同步人员", getPlatformName());
		List<JSONObject> depts = fetchDepartments();
		int[] counts = new int[2];
		for (JSONObject dept : depts) {
			String deptId = getDeptId(dept);
			String deptName = getDeptName(dept);
			if (StrUtil.isBlank(deptId)) {
				continue;
			}
			for (JSONObject user : fetchUsers(deptId)) {
				SyncUserDTO dto = convertToUser(user, deptId, deptName);
				if (upsertUser(dto)) {
					counts[0]++;
				}
				else {
					counts[1]++;
				}
			}
		}
		log.info("[{}] 人员同步完成: 新增 {}, 更新 {}", getPlatformName(), counts[0], counts[1]);
		return String.format("人员同步完成: 新增 %d, 更新 %d", counts[0], counts[1]);
	}

	/** 根据第三方用户ID查询本地员工记录ID */
	private String queryEmployeeIdByThirdUserId(String thirdUserId) {
		Row row = Db.selectOneBySql("SELECT id FROM tbl_privilege_employee WHERE third_user_id = ? AND del_flag = 0",
				thirdUserId);
		return row != null ? (String) row.get("id") : null;
	}

	/** 根据本地员工ID查询第三方用户ID */
	private String queryThirdUserIdByLocalEmployeeId(String employeeId) {
		if (StrUtil.isBlank(employeeId))
			return null;
		Row row = Db.selectOneBySql("SELECT third_user_id FROM tbl_privilege_employee WHERE id = ? AND del_flag = 0",
				employeeId);
		return row != null ? (String) row.get("third_user_id") : null;
	}

	/** 同步指定部门的下级及所有子级部门 */
	@Override
	public String syncSubDepartments(String deptId) {
		log.info("[{}] 开始同步下级部门, parentId: {}", getPlatformName(), deptId);
		String remoteDeptId = queryThirdIdByLocalDeptId(deptId);
		if (remoteDeptId == null) {
			log.warn("[{}] 未找到本地部门ID {} 对应的第三方部门ID", getPlatformName(), deptId);
			return "未找到对应的第三方部门";
		}
		String companyId = queryCompanyIdByLocalDeptId(deptId);
		List<JSONObject> allDepts = fetchDepartments();
		Map<String, List<JSONObject>> childrenMap = allDepts.stream()
			.filter(dept -> StrUtil.isNotBlank(getParentDeptId(dept)))
			.collect(Collectors.groupingBy(this::getParentDeptId));
		List<JSONObject> subDepts = new ArrayList<>();
		List<String> queue = new ArrayList<>();
		queue.add(remoteDeptId);
		while (!queue.isEmpty()) {
			String parentId = queue.remove(0);
			List<JSONObject> children = childrenMap.getOrDefault(parentId, Collections.emptyList());
			for (JSONObject child : children) {
				subDepts.add(child);
				queue.add(getDeptId(child));
			}
		}
		syncDeptToPrivilege(subDepts, companyId);
		log.info("[{}] 下级部门同步完成", getPlatformName());
		return "下级部门同步完成";
	}

	/** 同步指定部门下的人员 */
	@Override
	public String syncUsersByDept(String deptId) {
		log.info("[{}] 开始同步部门下人员, deptId: {}", getPlatformName(), deptId);
		String remoteDeptId = queryThirdIdByLocalDeptId(deptId);
		if (remoteDeptId == null) {
			log.warn("[{}] 未找到本地部门ID {} 对应的第三方部门ID", getPlatformName(), deptId);
			return "未找到对应的第三方部门";
		}
		List<JSONObject> users = fetchUsers(remoteDeptId);
		int[] counts = new int[2];
		String deptName = "";
		for (JSONObject user : users) {
			SyncUserDTO dto = convertToUser(user, remoteDeptId, deptName);
			if (upsertUser(dto)) {
				counts[0]++;
			}
			else {
				counts[1]++;
			}
		}
		log.info("[{}] 部门下人员同步完成: 新增 {}, 更新 {}", getPlatformName(), counts[0], counts[1]);
		return String.format("部门下人员同步完成: 新增 %d, 更新 %d", counts[0], counts[1]);
	}

	/** 根据本地员工ID同步单个人员信息（自动转换为第三方ID） */
	@Override
	public String syncUser(String employeeId) {
		log.info("[{}] 开始同步人员信息, employeeId: {}", getPlatformName(), employeeId);
		String thirdUserId = queryThirdUserIdByLocalEmployeeId(employeeId);
		if (thirdUserId == null) {
			log.warn("[{}] 未找到本地员工ID {} 对应的第三方用户ID", getPlatformName(), employeeId);
			return "未找到对应的第三方用户";
		}
		JSONObject userData = fetchSingleUser(thirdUserId);
		if (userData == null) {
			return "未找到该人员";
		}
		List<String> deptIds = getUserDeptIds(userData);
		String deptId = deptIds.isEmpty() ? "" : deptIds.get(0);
		String deptName = "";
		if (StrUtil.isNotBlank(deptId)) {
			Row row = Db.selectOneBySql("SELECT COALESCE(pd.name, pc.cname) AS name FROM tbl_privilege_department pd "
					+ "LEFT JOIN tbl_privilege_company pc ON pd.company_id = pc.id AND pc.del_flag = 0 "
					+ "WHERE pd.third_id = ? AND pd.del_flag = 0 " + "UNION ALL "
					+ "SELECT cname FROM tbl_privilege_company WHERE third_id = ? AND del_flag = 0 " + "LIMIT 1",
					deptId, deptId);
			deptName = row != null ? (String) row.get("name") : deptId;
		}
		upsertUser(convertToUser(userData, deptId, deptName));
		log.info("[{}] 人员信息同步完成: {}", getPlatformName(), thirdUserId);
		return "人员信息同步完成";
	}

	/**
	 * 新增或更新员工信息（按thirdUserId判断是否存在），同步更新tbl_privilege_user和tbl_platform_account_info（仅更新）
	 */
	private boolean upsertUser(SyncUserDTO dto) {
		if (StrUtil.isBlank(dto.getThirdUserId())) {
			return false;
		}
		String[] localRefs = resolveLocalDeptAndCompany(dto.getDeptId(), dto.getDeptName());
		String localDeptId = localRefs[0];
		String companyId = localRefs[1];
		String companyName = localRefs[2];
		String localDeptName = localRefs[3];

		String existId = queryEmployeeIdByThirdUserId(dto.getThirdUserId());
		boolean created = false;
		if (existId != null) {
			Db.updateBySql(
					"UPDATE tbl_privilege_employee SET emp_name = ?, mobile = ?, email = ?, avatar_url = ?, company_id = ?, company_name = ?, dept_id = ?, dept_name = ?, third_open_id = ?, third_union_id = ?, status = ? WHERE id = ?",
					dto.getRealName(), dto.getPhone(), dto.getEmail(), dto.getAvatarUrl(), companyId, companyName,
					localDeptId, localDeptName, dto.getThirdOpenId(), dto.getThirdUnionId(),
					dto.getStatus() != null ? dto.getStatus() : 1, existId);
		}
		else {
			String employeeId = IdUtil.getSnowflakeNextIdStr();
			Db.insertBySql(
					"INSERT INTO tbl_privilege_employee (id, emp_code, emp_name, mobile, email, avatar_url, company_id, company_name, dept_id, dept_name, third_user_id, third_open_id, third_union_id, status, enable_flag, del_flag) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 0)",
					employeeId, dto.getThirdUserId(), dto.getRealName(), dto.getPhone(), dto.getEmail(),
					dto.getAvatarUrl(), companyId, companyName, localDeptId, localDeptName, dto.getThirdUserId(),
					dto.getThirdOpenId(), dto.getThirdUnionId(), dto.getStatus() != null ? dto.getStatus() : 1);
			existId = employeeId;
			created = true;
		}
		// 同步更新 tbl_privilege_user（仅更新已存在的记录）
		if (StrUtil.isNotBlank(existId)) {
			int updated = Db.updateBySql(
					"UPDATE tbl_privilege_user SET real_name = ?, mobile = ?, email = ?, image = ?, company_id = ?, dept_id = ? WHERE employee_id = ? AND del_flag = 0",
					dto.getRealName(), dto.getPhone(), dto.getEmail(), dto.getAvatarUrl(), companyId, localDeptId,
					existId);
			if (updated > 0) {
				log.debug("[{}] 已同步更新tbl_privilege_user, employeeId: {}", getPlatformName(), existId);
			}
		}
		// 同步更新 tbl_platform_account_info（仅更新已存在的记录）
		if (StrUtil.isNotBlank(dto.getThirdPartyId())) {
			int updated = Db.updateBySql(
					"UPDATE tbl_platform_account_info SET real_name = ?, phone = ?, email = ?, avatar_url = ?, dept_id = ?, dept_name = ? WHERE third_party_id = ? AND del_flag = 1",
					dto.getRealName(), dto.getPhone(), dto.getEmail(), dto.getAvatarUrl(), dto.getDeptId(),
					dto.getDeptName(), dto.getThirdPartyId());
			if (updated > 0) {
				log.debug("[{}] 已同步更新tbl_platform_account_info, thirdPartyId: {}", getPlatformName(),
						dto.getThirdPartyId());
			}
		}
		return created;
	}

	/** 根据第三方部门ID+部门名称解析本地部门ID、公司ID、公司名称、部门名称，返回数组[deptId, companyId, companyName, deptName] */
	private String[] resolveLocalDeptAndCompany(String platformDeptId, String deptName) {
		String[] result = new String[] { null, null, null, null };
		if (StrUtil.isBlank(platformDeptId)) {
			return result;
		}
		Row row = Db
			.selectOneBySql("SELECT pd.id AS dept_id, pd.name AS dept_name, pd.company_id, pc.cname AS company_name "
					+ "FROM tbl_privilege_department pd "
					+ "LEFT JOIN tbl_privilege_company pc ON pd.company_id = pc.id AND pc.del_flag = 0 "
					+ "WHERE pd.third_id = ? AND pd.del_flag = 0", platformDeptId);
		if (row != null) {
			result[0] = (String) row.get("dept_id");
			result[1] = (String) row.get("company_id");
			result[2] = (String) row.get("company_name");
			result[3] = (String) row.get("dept_name");
		}
		else {
			// 按cname+third_id联合查询，避免多个公司有相同third_id时TooManyResults
			Row companyRow = null;
			if (StrUtil.isNotBlank(deptName)) {
				companyRow = Db.selectOneBySql(
						"SELECT id AS company_id, cname AS company_name FROM tbl_privilege_company WHERE cname = ? AND third_id = ? AND del_flag = 0",
						deptName, platformDeptId);
			}
			if (companyRow == null) {
				companyRow = Db.selectOneBySql(
						"SELECT id AS company_id, cname AS company_name FROM tbl_privilege_company WHERE third_id = ? AND del_flag = 0 LIMIT 1",
						platformDeptId);
			}
			if (companyRow != null) {
				result[1] = (String) companyRow.get("company_id");
				result[2] = (String) companyRow.get("company_name");
			}
			else {
				log.warn("[{}] 未能解析本地部门和公司ID, platformDeptId: {}", getPlatformName(), platformDeptId);
			}
		}
		return result;
	}

	/** 根据第三方用户ID查询本地员工记录ID */
	private String queryEmployeeId(String thirdUserId) {
		Row row = Db.selectOneBySql("SELECT id FROM tbl_privilege_employee WHERE third_user_id = ? AND del_flag = 0",
				thirdUserId);
		return row != null ? (String) row.get("id") : null;
	}

	// ========== 子类需实现的抽象方法 ==========

	/** 获取平台名称（如 钉钉、飞书、微信） */
	protected abstract String getPlatformName();

	/** 获取第三方所有部门列表 */
	protected abstract List<JSONObject> fetchDepartments();

	/** 获取指定部门下的人员列表 */
	protected abstract List<JSONObject> fetchUsers(String deptId);

	/** 从部门对象中提取部门ID */
	protected abstract String getDeptId(JSONObject dept);

	/** 从部门对象中提取部门名称 */
	protected abstract String getDeptName(JSONObject dept);

	/** 从部门对象中提取父部门ID */
	protected abstract String getParentDeptId(JSONObject dept);

	/** 判断部门是否为根部门（公司级） */
	protected abstract boolean isRootDept(JSONObject dept);

	/** 将第三方部门对象转换为本地SyncDeptDTO */
	protected abstract SyncDeptDTO convertToDept(JSONObject dept);

	/** 将第三方用户对象转换为本地SyncUserDTO */
	protected abstract SyncUserDTO convertToUser(JSONObject user, String deptId, String deptName);

	/** 根据第三方用户ID获取单个用户详情 */
	protected abstract JSONObject fetchSingleUser(String thirdPartyId);

	/** 从用户对象中获取所属部门ID列表 */
	protected abstract List<String> getUserDeptIds(JSONObject user);

}
