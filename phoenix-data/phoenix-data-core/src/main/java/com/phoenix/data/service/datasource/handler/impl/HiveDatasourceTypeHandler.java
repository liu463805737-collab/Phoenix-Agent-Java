package com.phoenix.data.service.datasource.handler.impl;

import com.phoenix.data.entity.Datasource;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import com.phoenix.data.service.datasource.handler.DatasourceTypeHandler;
import org.springframework.stereotype.Component;

/**
 * Hive 数据源类型处理器
 */
@Component
public class HiveDatasourceTypeHandler implements DatasourceTypeHandler {

	/**
	 * 返回Hive数据库类型名称
	 * @return 类型名称
	 */
	@Override
	public String typeName() {
		return BizDataSourceTypeEnum.HIVE.getTypeName();
	}

	/**
	 * 构建Hive JDBC连接URL
	 * @param datasource 数据源
	 * @return 连接URL
	 */
	@Override
	public String buildConnectionUrl(Datasource datasource) {
		if (!hasRequiredConnectionFields(datasource)) {
			return datasource.getConnectionUrl();
		}

		return String.format("jdbc:hive2://%s:%d/%s", datasource.getHost(), datasource.getPort(),
				datasource.getDatabaseName());
	}

	/**
	 * Hive测试连接URL不需要额外参数
	 * @param datasource 数据源
	 * @param url 原始URL
	 * @return 规范化后的URL
	 */
	@Override
	public String normalizeTestUrl(Datasource datasource, String url) {
		return url;
	}

}
