package com.phoenix.privilege.listener;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import com.phoenix.common.model.BaseModel;
import com.phoenix.privilege.base.BaseEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class BaseEntityInsertFillListener implements InsertListener, UpdateListener {

	@Override
	public void onInsert(Object entity) {
		String username = getCurrentUserId();
		if (entity instanceof BaseEntity base) {
			base.setCreateBy(username);
			base.setCreateTime(new Date());
		}
		if (entity instanceof BaseModel base) {
			base.setCreator(username);
		}
	}

	@Override
	public void onUpdate(Object entity) {
		String userId = getCurrentUserId();
		if (entity instanceof BaseEntity base) {
			base.setUpdateTime(new Date());
			base.setUpdateBy(userId);
		}
	}

	private String getCurrentUserId() {
		try {
			return (String) StpUtil.getSession().getLoginId();
		}
		catch (Exception e) {
			log.trace("No login context, skip fill creator");
		}
		return "SYSTEM";
	}

}
