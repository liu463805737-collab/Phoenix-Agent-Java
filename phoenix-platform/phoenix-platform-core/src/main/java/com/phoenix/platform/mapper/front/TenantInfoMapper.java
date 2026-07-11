package com.phoenix.platform.mapper.front;

import com.mybatisflex.core.BaseMapper;
import com.phoenix.platform.model.front.TenantInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户信息 Mapper
 * <p>
 * 继承 MyBatis-Plus {@link BaseMapper}，自动获得基础 CRUD 能力。
 */
@Mapper
public interface TenantInfoMapper extends BaseMapper<TenantInfo> {
}
