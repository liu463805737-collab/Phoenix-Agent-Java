package com.phoenix.platform.service.thirdparty.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.platform.mapper.thirdparty.DingtalkMapper;

import com.phoenix.platform.model.thirdparty.DingtalkEntity;
import com.phoenix.platform.service.thirdparty.DingtalkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 钉钉用户信息服务实现
 * <p>
 * 继承 MyBatis-Flex {@link ServiceImpl}，自动获得基础 CRUD 能力。
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DingtalkServiceImpl extends ServiceImpl<DingtalkMapper, DingtalkEntity> implements DingtalkService {

	@Override
	public Page<DingtalkEntity> page(Page<DingtalkEntity> page, DingtalkEntity query) {
		return QueryChain.of(getMapper())
			.like(DingtalkEntity::getUserCode, query.getUserCode(), StrUtil.isNotBlank(query.getUserCode()))
			.like(DingtalkEntity::getUserName, query.getUserName(), StrUtil.isNotBlank(query.getUserName()))
			.orderBy(DingtalkEntity::getCreateTime, false)
			.page(page);
	}

	@Override
	public DingtalkEntity getByUserCode(String userCode) {
		return QueryChain.of(getMapper()).eq(DingtalkEntity::getUserCode, userCode).one();
	}

	@Override
	public List<DingtalkEntity> getByUserIds(List<String> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return Collections.emptyList();
		}
		return QueryChain.of(getMapper()).in(DingtalkEntity::getUserid, userIds).list();
	}

}
