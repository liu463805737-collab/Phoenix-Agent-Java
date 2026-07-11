package com.phoenix.common.service.platform.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.common.mapper.platform.PlatformInfoMapper;
import com.phoenix.common.model.platform.PlatformInfo;
import com.phoenix.common.service.platform.PlatformInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class PlatformInfoServiceImpl extends ServiceImpl<PlatformInfoMapper, PlatformInfo> implements PlatformInfoService {

    @Override
    public Page<PlatformInfo> page(Page<PlatformInfo> page, PlatformInfo query) {
        return QueryChain.of(getMapper())
            .like(PlatformInfo::getName, query.getName(), StrUtil.isNotBlank(query.getName()))
            .eq(PlatformInfo::getType, query.getType(), StrUtil.isNotBlank(query.getType()))
            .eq(PlatformInfo::getStatus, query.getStatus(), StrUtil.isNotBlank(query.getStatus()))
            .orderBy(PlatformInfo::getCreateTime, false)
            .page(page);
    }

    @Override
    public PlatformInfo getByType(String type) {
        return QueryChain.of(getMapper()).eq(PlatformInfo::getType, type).one();
    }

    @Override
    public PlatformInfo getEnabledByType(String type) {
        return QueryChain.of(getMapper())
            .eq(PlatformInfo::getType, type)
            .eq(PlatformInfo::getStatus, "1")
            .one();
    }

    @Override
    public PlatformInfo getEnabled() {
        return QueryChain.of(getMapper())
                .eq(PlatformInfo::getStatus, "1")
                .one();
    }

}
