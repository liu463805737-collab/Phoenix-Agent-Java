package com.phoenix.data.service.datasource.handler.impl;

import com.phoenix.data.entity.Datasource;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import com.phoenix.data.service.datasource.handler.DatasourceTypeHandler;
import org.springframework.stereotype.Component;

/**
 * 达梦数据库数据源类型处理器
 */
@Component
public class DamengDatasourceTypeHandler implements DatasourceTypeHandler {

	/**
	 * 返回达梦数据库类型名称
	 * @return 类型名称
	 */
	@Override
	public String typeName() {
		return BizDataSourceTypeEnum.DAMENG.getTypeName();
	}

	/**
	 * 构建达梦数据库JDBC连接URL
	 * @param datasource 数据源
	 * @return 连接URL
	 */
	@Override
	public String buildConnectionUrl(Datasource datasource) {
		if (!hasRequiredConnectionFields(datasource)) {
			return datasource.getConnectionUrl();
		}
		return String.format("jdbc:dm://%s:%d", datasource.getHost(), datasource.getPort());
	}

}
