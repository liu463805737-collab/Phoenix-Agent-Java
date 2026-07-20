package com.phoenix.common.service.sync.dingtalk;

public interface DingTalkSyncConstants {

	String BASE_URL = "https://oapi.dingtalk.com";

	String TOKEN_URL = BASE_URL + "/gettoken";

	String DEPT_GET_URL = BASE_URL + "/topapi/v2/department/get";

	String DEPT_LIST_SUB_URL = BASE_URL + "/topapi/v2/department/listsub";

	String USER_LIST_URL = BASE_URL + "/topapi/v2/user/list";

	String USER_GET_URL = BASE_URL + "/topapi/v2/user/get";

	String USER_INFO_URL = BASE_URL + "/topapi/v2/user/getuserinfo";

}
