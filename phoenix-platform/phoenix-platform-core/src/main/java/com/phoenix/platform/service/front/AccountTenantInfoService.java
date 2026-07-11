package com.phoenix.platform.service.front;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.phoenix.platform.model.front.AccountTenantInfo;
import com.phoenix.platform.model.front.TenantInfo;

import java.util.List;

public interface AccountTenantInfoService extends IService<AccountTenantInfo> {

    Page<AccountTenantInfo> page(Page<AccountTenantInfo> page, AccountTenantInfo query);

    List<TenantInfo> getByAccountId(String accountId);
}
