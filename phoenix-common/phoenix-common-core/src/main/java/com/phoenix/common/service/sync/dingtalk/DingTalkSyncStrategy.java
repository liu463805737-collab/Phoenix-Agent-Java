package com.phoenix.common.service.sync.dingtalk;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.phoenix.common.model.dto.SyncDeptDTO;
import com.phoenix.common.model.dto.SyncUserDTO;
import com.phoenix.common.service.platform.DingTalkSdkService;
import com.phoenix.common.service.sync.AbstractPlatformSyncStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DingTalkSyncStrategy extends AbstractPlatformSyncStrategy {

	private final DingTalkSdkService dingTalkSdkService;

	public DingTalkSyncStrategy(DingTalkSdkService dingTalkSdkService) {
		this.dingTalkSdkService = dingTalkSdkService;
	}

	@Override
	protected String getPlatformName() {
		return "钉钉";
	}

	@Override
	protected List<JSONObject> fetchDepartments() {
		return dingTalkSdkService.getDepartments();
	}

	@Override
	protected List<JSONObject> fetchUsers(String deptId) {
		return dingTalkSdkService.getUsersByDepartmentId(deptId);
	}

	@Override
	protected String getDeptId(JSONObject dept) {
		return dept.getStr("dept_id");
	}

	@Override
	protected String getDeptName(JSONObject dept) {
		return dept.getStr("name");
	}

	@Override
	protected String getParentDeptId(JSONObject dept) {
		return dept.getStr("parent_id");
	}

	@Override
	protected boolean isRootDept(JSONObject dept) {
		return "1".equals(dept.getStr("dept_id"));
	}

	@Override
	protected SyncDeptDTO convertToDept(JSONObject dept) {
		return SyncDeptDTO.builder()
			.sn(dept.getStr("dept_id"))
			.name(dept.getStr("name"))
			.status(0)
			.parentSn(dept.getStr("parent_id"))
			.root(isRootDept(dept))
			.build();
	}

	@Override
	protected SyncUserDTO convertToUser(JSONObject user, String deptId, String deptName) {
		String userid = user.getStr("userid");
		return SyncUserDTO.builder()
			.thirdPartyId(userid)
			.username(userid)
			.realName(user.getStr("name"))
			.phone(user.getStr("mobile"))
			.email(user.getStr("email"))
			.avatarUrl(user.getStr("avatar"))
			.status(convertStatus(user.getInt("status")))
			.deptId(deptId)
			.deptName(deptName)
			.thirdUserId(userid)
			.thirdOpenId(userid)
			.thirdUnionId(user.getStr("unionid"))
			.build();
	}

	@Override
	protected JSONObject fetchSingleUser(String thirdPartyId) {
		String token = dingTalkSdkService.getAccessToken();
		String url = DingTalkSyncConstants.USER_GET_URL + token;
		String body = JSONUtil.createObj().set("userid", thirdPartyId).toString();
		String resp = HttpRequest.post(url).body(body).execute().body();
		JSONObject result = JSONUtil.parseObj(resp);
		Integer errcode = result.getInt("errcode");
		if (errcode == null || errcode != 0) {
			throw new RuntimeException("获取钉钉人员信息失败: " + result.getStr("errmsg"));
		}
		return result.getJSONObject("result");
	}

	@Override
	protected List<String> getUserDeptIds(JSONObject user) {
		return user.getJSONArray("dept_id_list").toList(String.class);
	}

	/** 钉钉状态转本地状态：1在职->1，其他->0 */
	private Integer convertStatus(Integer dingtalkStatus) {
		if (dingtalkStatus == null)
			return null;
		return dingtalkStatus == 1 ? 1 : 0;
	}

}
