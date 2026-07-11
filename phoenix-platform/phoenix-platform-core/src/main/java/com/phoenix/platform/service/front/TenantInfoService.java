package com.phoenix.platform.service.front;


import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.phoenix.platform.model.front.TenantInfo;

/**
 * 租户信息服务接口
 * <p>
 * 继承 MyBatis-Plus {@link IService}，自动获得基础 CRUD 能力。
 */
public interface TenantInfoService extends IService<TenantInfo> {

    /**
     * 分页查询租户信息
     *
     * @param page  分页参数（页码、每页大小）
     * @param query 查询条件（name/sn 模糊匹配）
     * @return 分页结果
     */
    Page<TenantInfo> page(Page<TenantInfo> page, TenantInfo query);

    /**
     * 根据 sn 编码查询租户信息
     *
     * @param sn 租户编码
     * @return 租户信息，未找到返回 null
     */
    TenantInfo getBySn(String sn);
}
