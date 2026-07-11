package com.phoenix.agent.service.store.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.agent.mapper.CombinedStoreMapper;
import com.phoenix.agent.model.CombinedStore;
import com.phoenix.agent.service.store.CombinedStoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class CombinedStoreServiceImpl extends ServiceImpl<CombinedStoreMapper, CombinedStore> implements CombinedStoreService {
    @Override
    public void initTable() {
        mapper.initTable();
    }
}
