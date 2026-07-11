package com.phoenix.data.service.datasource.handler.impl;

import com.phoenix.data.entity.Datasource;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import com.phoenix.data.service.datasource.handler.DatasourceTypeHandler;
import org.springframework.stereotype.Component;

/**
 * PostgreSQL 数据源类型处理器
 */
@Component
public class PostgreSqlDatasourceTypeHandler implements DatasourceTypeHandler {

	/**
	 * 返回PostgreSQL数据库类型名称
	 * @return 类型名称
	 */
	@Override
	public String typeName() {
		return BizDataSourceTypeEnum.POSTGRESQL.getTypeName();
	}

	/**
	 * 构建PostgreSQL JDBC连接URL
	 * @param datasource 数据源
	 * @return 连接URL
	 */
	@Override
	public String buildConnectionUrl(Datasource datasource) {
		if (!hasRequiredConnectionFields(datasource)) {
			return datasource.getConnectionUrl();
		}
		// 提取数据库名（format: "database|schema"，只取database部分）
		String databaseName = datasource.getDatabaseName();
		if (databaseName != null && databaseName.contains("|")) {
			databaseName = databaseName.split("\\|")[0];
		}
		return String.format(
				"jdbc:postgresql://%s:%d/%s?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai",
				datasource.getHost(), datasource.getPort(), databaseName);
	}

	/**
	 * 提取PostgreSQL的Schema名称，格式为 "database|schema"
	 * @param datasource 数据源
	 * @return Schema名称
	 */
	@Override
	public String extractSchemaName(Datasource datasource) {
		// 提取schema名（format: "database|schema"，取schema部分）
		String databaseName = datasource.getDatabaseName();
		if (databaseName != null && databaseName.contains("|")) {
			String[] parts = databaseName.split("\\|");
			return parts.length > 1 ? parts[1] : parts[0];
		}
		return databaseName;
	}

}
