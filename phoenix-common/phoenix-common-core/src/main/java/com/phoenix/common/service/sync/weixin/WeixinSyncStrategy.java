package com.phoenix.common.service.sync.weixin;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.phoenix.common.model.dto.SyncDeptDTO;
import com.phoenix.common.model.dto.SyncUserDTO;
import com.phoenix.common.service.platform.WeixinSdkService;
import com.phoenix.common.service.sync.AbstractPlatformSyncStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WeixinSyncStrategy extends AbstractPlatformSyncStrategy {

	private final WeixinSdkService weixinSdkService;

	public WeixinSyncStrategy(WeixinSdkService weixinSdkService) {
		this.weixinSdkService = weixinSdkService;
	}

	@Override
	protected String getPlatformName() {
		return "企业微信";
	}

	@Override
	protected List<JSONObject> fetchDepartments() {
		return weixinSdkService.getDepartments();
	}

	@Override
	protected List<JSONObject> fetchUsers(String deptId) {
		return weixinSdkService.getUsersByDepartmentId(deptId);
	}

	@Override
	protected String getDeptId(JSONObject dept) {
		return dept.getStr("id");
	}

	@Override
	protected String getDeptName(JSONObject dept) {
		return dept.getStr("name");
	}

	@Override
	protected String getParentDeptId(JSONObject dept) {
		return dept.getStr("parentid");
	}

	@Override
	protected boolean isRootDept(JSONObject dept) {
		return "0".equals(dept.getStr("parentid"));
	}

	@Override
	protected SyncDeptDTO convertToDept(JSONObject dept) {
		return SyncDeptDTO.builder()
			.sn(dept.getStr("id"))
			.name(dept.getStr("name"))
			.status(0)
			.parentSn(dept.getStr("parentid"))
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
			.build();
	}

	@Override
	protected JSONObject fetchSingleUser(String thirdPartyId) {
		String token = weixinSdkService.getAccessToken();
		String url = WeixinSyncConstants.USER_GET_URL + token + "&userid=" + thirdPartyId;
		String resp = HttpRequest.get(url).execute().body();
		JSONObject result = JSONUtil.parseObj(resp);
		Integer errcode = result.getInt("errcode");
		if (errcode == null || errcode != 0) {
			throw new RuntimeException("获取企业微信人员信息失败: " + result.getStr("errmsg"));
		}
		return result;
	}

	@Override
	protected List<String> getUserDeptIds(JSONObject user) {
		return user.getJSONArray("department").toList(String.class);
	}

	/** 企业微信状态转本地状态：1激活->1，2禁用->0，4未激活->1 */
	private Integer convertStatus(Integer weixinStatus) {
		if (weixinStatus == null)
			return null;
		return weixinStatus == 2 ? 0 : 1;
	}

}
