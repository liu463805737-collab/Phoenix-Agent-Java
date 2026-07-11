package com.phoenix.platform.service.front.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.platform.mapper.front.TenantInfoMapper;
import com.phoenix.platform.model.front.TenantInfo;
import com.phoenix.platform.service.front.TenantInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class TenantInfoServiceImpl extends ServiceImpl<TenantInfoMapper, TenantInfo> implements TenantInfoService {

    @Override
    public Page<TenantInfo> page(Page<TenantInfo> page, TenantInfo query) {
        return QueryChain.of(getMapper())
                .like(TenantInfo::getName, query.getName(), StrUtil.isNotBlank(query.getName()))
                .like(TenantInfo::getSn, query.getSn(), StrUtil.isNotBlank(query.getSn()))
                .orderBy(TenantInfo::getCreateTime, false)
                .page(page);
    }

    @Override
    public TenantInfo getBySn(String sn) {
        return QueryChain.of(getMapper())
                .eq(TenantInfo::getSn, sn)
                .one();
    }
}
