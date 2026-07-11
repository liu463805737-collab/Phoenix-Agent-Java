package com.phoenix.agent.service.store;


import com.mybatisflex.core.service.IService;
import com.phoenix.agent.model.CombinedStore;

/**
 * 长期记忆服务
 */
public interface CombinedStoreService extends IService<CombinedStore> {
    /**
     * 初始化脚本
     */
    void initTable() ;
}
