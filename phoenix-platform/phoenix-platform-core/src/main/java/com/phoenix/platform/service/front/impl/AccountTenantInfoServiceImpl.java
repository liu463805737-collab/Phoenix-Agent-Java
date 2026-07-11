package com.phoenix.platform.service.front.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.platform.mapper.front.AccountTenantInfoMapper;
import com.phoenix.platform.model.front.AccountTenantInfo;
import com.phoenix.platform.model.front.TenantInfo;
import com.phoenix.platform.service.front.AccountTenantInfoService;
import com.phoenix.platform.service.front.TenantInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AccountTenantInfoServiceImpl extends ServiceImpl<AccountTenantInfoMapper, AccountTenantInfo> implements AccountTenantInfoService {

    private final TenantInfoService tenantInfoService;

    @Override
    public Page<AccountTenantInfo> page(Page<AccountTenantInfo> page, AccountTenantInfo query) {
        return QueryChain.of(getMapper())
                .like(AccountTenantInfo::getAccountId, query.getAccountId(), StrUtil.isNotBlank(query.getAccountId()))
                .like(AccountTenantInfo::getTenantId, query.getTenantId(), StrUtil.isNotBlank(query.getTenantId()))
                .orderBy(AccountTenantInfo::getCreateTime, false)
                .page(page);
    }

    @Override
    public List<TenantInfo> getByAccountId(String accountId) {
        List<AccountTenantInfo> list = getMapper().selectListByQuery(
                QueryWrapper.create()
                        .select(AccountTenantInfo::getTenantId)
                        .where(AccountTenantInfo::getAccountId).eq(accountId)
        );
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<String> tenantIds = list.stream()
                .map(AccountTenantInfo::getTenantId)
                .distinct()
                .collect(Collectors.toList());
        return tenantInfoService.listByIds(tenantIds);
    }
}
