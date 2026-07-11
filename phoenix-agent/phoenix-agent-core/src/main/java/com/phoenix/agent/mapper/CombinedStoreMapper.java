package com.phoenix.agent.mapper;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.agent.model.CombinedStore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CombinedStoreMapper extends BaseMapper<CombinedStore> {

    /**
     * 初始化脚本
     */
    @Update("""
            CREATE TABLE IF NOT EXISTS tbl_vector_store_combined (
                    id TEXT PRIMARY KEY,
                    namespace TEXT,
                    key_name VARCHAR(500),
                      value_json TEXT,
                      created_at TIMESTAMP,
                      updated_at TIMESTAMP
                      );
            """)
    void initTable() ;
}
