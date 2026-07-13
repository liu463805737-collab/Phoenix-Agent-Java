package com.phoenix.common.service.sync.feishu;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.phoenix.common.model.dto.SyncDeptDTO;
import com.phoenix.common.model.dto.SyncUserDTO;
import com.phoenix.common.service.platform.FeishuSdkService;
import com.phoenix.common.service.sync.AbstractPlatformSyncStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FeishuSyncStrategy extends AbstractPlatformSyncStrategy {

	private final FeishuSdkService feishuSdkService;

	public FeishuSyncStrategy(FeishuSdkService feishuSdkService) {
		this.feishuSdkService = feishuSdkService;
	}

	@Override
	protected String getPlatformName() {
		return "飞书";
	}

	@Override
	protected List<JSONObject> fetchDepartments() {
		return feishuSdkService.getDepartments();
	}

	@Override
	protected List<JSONObject> fetchUsers(String deptId) {
		return feishuSdkService.getUsersByDepartmentId(deptId);
	}

	@Override
	protected String getDeptId(JSONObject dept) {
		return dept.getStr("department_id");
	}

	@Override
	protected String getDeptName(JSONObject dept) {
		return dept.getStr("name");
	}

	@Override
	protected String getParentDeptId(JSONObject dept) {
		return dept.getStr("parent_department_id");
	}

	@Override
	protected boolean isRootDept(JSONObject dept) {
		String parent = dept.getStr("parent_department_id");
		return StrUtil.isBlank(parent);
	}

	@Override
	protected SyncDeptDTO convertToDept(JSONObject dept) {
		return SyncDeptDTO.builder()
			.sn(dept.getStr("department_id"))
			.name(dept.getStr("name"))
			.status(0)
			.parentSn(dept.getStr("parent_department_id"))
			.root(isRootDept(dept))
			.build();
	}

	@Override
	protected SyncUserDTO convertToUser(JSONObject user, String deptId, String deptName) {
		String userId = user.getStr("user_id");
		return SyncUserDTO.builder()
			.thirdPartyId(userId)
			.username(userId)
			.realName(user.getStr("name"))
			.phone(user.getStr("mobile"))
			.email(user.getStr("email"))
			.avatarUrl(user.getStr("avatar"))
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
