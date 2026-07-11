package com.phoenix.privilege.config;

import com.mybatisflex.core.FlexGlobalConfig;
import com.phoenix.privilege.base.BaseEntity;
import com.phoenix.privilege.listener.BaseEntityInsertFillListener;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

/**
 * 给实体类充填基础字段
 */
@Configuration
public class MyBatisFlexFillConfig {

	@PostConstruct
	public void registerFillListeners() {
		FlexGlobalConfig config = FlexGlobalConfig.getDefaultConfig();
		config.registerInsertListener(new BaseEntityInsertFillListener(), BaseEntity.class);
		config.registerUpdateListener(new BaseEntityInsertFillListener(), BaseEntity.class);
	}

}
