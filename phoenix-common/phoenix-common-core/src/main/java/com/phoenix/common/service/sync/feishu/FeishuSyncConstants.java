package com.phoenix.common.service.sync.feishu;

public interface FeishuSyncConstants {

	String BASE_URL = "https://open.feishu.cn/open-apis";

	String TOKEN_URL = BASE_URL + "/auth/v3/tenant_access_token/internal";

	String DEPT_LIST_URL = BASE_URL + "/contact/v3/departments";

	String USER_LIST_URL = BASE_URL + "/contact/v3/users";

	String USER_GET_URL = BASE_URL + "/contact/v3/users/";

	String TENANT_QUERY_URL = BASE_URL + "/tenant/v2/tenant/query";

	String OAUTH_ACCESS_TOKEN_URL = BASE_URL + "/authen/v1/oauth_access_token";

}
