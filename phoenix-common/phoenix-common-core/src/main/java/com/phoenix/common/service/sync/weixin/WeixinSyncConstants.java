package com.phoenix.common.service.sync.weixin;

public interface WeixinSyncConstants {

	String BASE_URL = "https://qyapi.weixin.qq.com";

	String TOKEN_URL = BASE_URL + "/cgi-bin/gettoken";

	String DEPT_LIST_URL = BASE_URL + "/cgi-bin/department/list?access_token=";

	String USER_LIST_URL = BASE_URL + "/cgi-bin/user/list?access_token=";

	String USER_GET_URL = BASE_URL + "/cgi-bin/user/get?access_token=";

	String USER_INFO_URL = BASE_URL + "/cgi-bin/user/getuserinfo";

}
