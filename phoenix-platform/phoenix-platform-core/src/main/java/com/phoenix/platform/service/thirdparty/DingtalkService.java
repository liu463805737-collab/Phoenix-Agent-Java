package com.phoenix.platform.service.thirdparty;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.phoenix.platform.model.thirdparty.DingtalkEntity;

import java.util.List;

/**
 * 钉钉用户信息服务接口
 * <p>
 * 继承 MyBatis-Flex {@link IService}，自动获得基础 CRUD 能力。
 */
public interface DingtalkService extends IService<DingtalkEntity> {

	/**
	 * 分页查询钉钉用户信息
	 * @param page 分页参数（页码、每页大小）
	 * @param query 查询条件（userCode/userName 模糊匹配）
	 * @return 分页结果
	 */
	Page<DingtalkEntity> page(Page<DingtalkEntity> page, DingtalkEntity query);

	/**
	 * 根据 userCode 查询钉钉用户信息
	 * @param userCode 用户编码
	 * @return 钉钉用户信息，未找到返回 null
	 */
	DingtalkEntity getByUserCode(String userCode);

	/**
	 * 根据用户 ID 列表批量查询钉钉用户信息
	 * @param userIds 钉钉用户 ID 列表
	 * @return 钉钉用户信息列表
	 */
	List<DingtalkEntity> getByUserIds(List<String> userIds);

}
