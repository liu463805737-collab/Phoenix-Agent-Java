package com.phoenix.platform.service.front;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.phoenix.platform.model.front.GroupInfo;

import java.util.List;

/**
 * 组织信息服务接口
 */
public interface GroupInfoService extends IService<GroupInfo> {

	Page<GroupInfo> page(Page<GroupInfo> page, GroupInfo query);

	GroupInfo getBySn(String sn);

	boolean deleteById(String id);

	/**
	 * 通过登录这的id获取他的用户组列表
	 * @param loginId 登录id
	 * @return
	 */
	List<GroupInfo> getByLoginId(String loginId);

}
