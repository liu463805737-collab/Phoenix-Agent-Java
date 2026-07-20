package com.phoenix.common.service.sync.feishu;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.phoenix.common.model.dto.SyncDeptDTO;
import com.phoenix.common.model.dto.SyncUserDTO;
import com.phoenix.common.service.platform.FeishuSdkService;
import com.phoenix.common.service.sync.AbstractPlatformSyncStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class FeishuSyncStrategy extends AbstractPlatformSyncStrategy {

	private final FeishuSdkService feishuSdkService;

	private String tenantName;
	private String tenantDisplayId;

	public FeishuSyncStrategy(FeishuSdkService feishuSdkService) {
		this.feishuSdkService = feishuSdkService;
	}

	@Override
	protected String getPlatformName() {
		return "飞书";
	}

	/** 获取或缓存企业信息，在公司部门id前拼接一个虚拟根节点 */
	private void ensureTenantInfo() {
		if (tenantName != null) return;
		try {
			JSONObject tenantInfo = feishuSdkService.getTenantInfo();
			tenantName = tenantInfo.getStr("name");
			tenantDisplayId = tenantInfo.getStr("display_id");
			if (StrUtil.isBlank(tenantName)) {
				tenantName = tenantDisplayId;
			}
			if (StrUtil.isBlank(tenantDisplayId)) {
				tenantDisplayId = "feishu_tenant";
			}
		} catch (Exception e) {
			log.warn("[飞书] 获取企业信息失败，使用默认名称", e);
			tenantName = "飞书企业";
			tenantDisplayId = "feishu_tenant";
		}
	}

	@Override
	protected List<JSONObject> fetchDepartments() {
		List<JSONObject> depts = feishuSdkService.getDepartments();
		ensureTenantInfo();
		// 在列表头部插入一个虚拟公司节点（根部门），用于创建公司记录
		JSONObject companyDept = new JSONObject();
		companyDept.set("open_department_id", tenantDisplayId);
		companyDept.set("name", tenantName);
		companyDept.set("parent_department_id", null);
		depts.add(0, companyDept);
		return depts;
	}

	@Override
	protected List<JSONObject> fetchUsers(String deptId) {
		return feishuSdkService.getUsersByDepartmentId(deptId);
	}

	@Override
	protected String getDeptId(JSONObject dept) {
		// 虚拟公司节点没有真实部门ID，返回 null 以跳过人员同步
		if (dept.get("parent_department_id") == null) return null;
		String id = dept.getStr("open_department_id");
		return StrUtil.isNotBlank(id) ? id : dept.getStr("department_id");
	}

	@Override
	protected String getDeptName(JSONObject dept) {
		return dept.getStr("name");
	}

	@Override
	protected String getParentDeptId(JSONObject dept) {
		ensureTenantInfo();
		String parent = dept.getStr("parent_department_id");
		if (StrUtil.isBlank(parent)) {
			return null; // 虚拟公司节点没有父部门
		}
		if ("0".equals(parent)) {
			return tenantDisplayId; // 飞书根级部门的父部门指向公司
		}
		return parent;
	}

	@Override
	protected boolean isRootDept(JSONObject dept) {
		String parent = dept.getStr("parent_department_id");
		return StrUtil.isBlank(parent); // 只有虚拟公司节点是根
	}

	@Override
	protected SyncDeptDTO convertToDept(JSONObject dept) {
		ensureTenantInfo();
		String deptId = dept.getStr("open_department_id");
		if (StrUtil.isBlank(deptId)) {
			deptId = dept.getStr("department_id");
		}
		String parentId = dept.getStr("parent_department_id");
		boolean root = isRootDept(dept);
		String parentSn;
		if (root) {
			parentSn = null;
		} else if (StrUtil.isBlank(parentId) || "0".equals(parentId)) {
			parentSn = tenantDisplayId;
		} else {
			parentSn = parentId;
		}
		return SyncDeptDTO.builder()
			.sn(deptId)
			.name(dept.getStr("name"))
			.status(0)
			.parentSn(parentSn)
			.root(root)
			.build();
	}

	@Override
	protected SyncUserDTO convertToUser(JSONObject user, String deptId, String deptName) {
		String userId = user.getStr("user_id");
		String avatarUrl = resolveAvatarUrl(user);
		return SyncUserDTO.builder()
			.thirdPartyId(userId)
			.username(userId)
			.realName(user.getStr("name"))
			.phone(user.getStr("mobile"))
			.email(user.getStr("email"))
			.avatarUrl(avatarUrl)
			.status(convertStatus(user))
			.deptId(deptId)
			.deptName(deptName)
			.thirdUserId(userId)
			.thirdOpenId(user.getStr("open_id"))
			.thirdUnionId(user.getStr("union_id"))
			.build();
	}

	@Override
	protected JSONObject fetchSingleUser(String thirdPartyId) {
		String token = feishuSdkService.getAccessToken();
		String url = FeishuSyncConstants.USER_GET_URL + thirdPartyId;
		String resp = HttpRequest.get(url).header("Authorization", "Bearer " + token).execute().body();
		JSONObject result = JSONUtil.parseObj(resp);
		Integer code = result.getInt("code");
		if (code == null || code != 0) {
			throw new RuntimeException("获取飞书人员信息失败: " + result.getStr("msg"));
		}
		return result.getJSONObject("data").getJSONObject("user");
	}

	@Override
	protected List<String> getUserDeptIds(JSONObject user) {
		return user.getJSONArray("departments").toList(String.class);
	}

	/** 飞书头像是一个对象（avatar_origin/avatar_72/avatar_240/avatar_640），取原图 */
	private static String resolveAvatarUrl(JSONObject user) {
		JSONObject avatar = user.getJSONObject("avatar");
		if (avatar != null) {
			return avatar.getStr("avatar_origin");
		}
		return null;
	}

	/** 飞书状态转本地状态：status.is_resigned=true->0, status.is_frozen=true->0, 否则->1 */
	private Integer convertStatus(JSONObject user) {
		JSONObject status = user.getJSONObject("status");
		if (status == null)
			return null;
		if (Boolean.TRUE.equals(status.getBool("is_resigned")) || Boolean.TRUE.equals(status.getBool("is_frozen"))) {
			return 0;
		}
		return 1;
	}

}
