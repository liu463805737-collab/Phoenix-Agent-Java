package com.phoenix.data.service.datasource.handler.impl;

import com.phoenix.data.entity.Datasource;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import com.phoenix.data.service.datasource.handler.DatasourceTypeHandler;
import org.springframework.stereotype.Component;

/**
 * Oracle 数据源类型处理器
 */
@Component
public class OracleDatasourceTypeHandler implements DatasourceTypeHandler {

	/**
	 * 返回Oracle数据库类型名称
	 * @return 类型名称
	 */
	@Override
	public String typeName() {
		return BizDataSourceTypeEnum.ORACLE.getTypeName();
	}

	/**
	 * 构建Oracle JDBC连接URL
	 * @param datasource 数据源
	 * @return 连接URL
	 */
	@Override
	public String buildConnectionUrl(Datasource datasource) {
		if (!hasRequiredConnectionFields(datasource)) {
			return datasource.getConnectionUrl();
		}
		// Oracle JDBC URL format: jdbc:oracle:thin:@host:port/serviceName
		return String.format("jdbc:oracle:thin:@%s:%d/%s", datasource.getHost(), datasource.getPort(),
				datasource.getDatabaseName());
	}

	/**
	 * Oracle测试连接URL不需要额外参数
	 * @param datasource 数据源
	 * @param url 原始URL
	 * @return 规范化后的URL
	 */
	@Override
	public String normalizeTestUrl(Datasource datasource, String url) {
		// Oracle doesn't require additional parameters for basic connectivity
		return url;
	}

	/**
	 * 提取Oracle数据库的Schema名称，格式为 "serviceName|schemaName"
	 * @param datasource 数据源
	 * @return Schema名称，未指定则返回null
	 */
	@Override
	public String extractSchemaName(Datasource datasource) {
		// For Oracle, schema is stored in databaseName as "serviceName|schemaName"
		// Extract the schema part after the | separator
		String databaseName = datasource.getDatabaseName();
		if (databaseName != null && databaseName.contains("|")) {
			String[] parts = databaseName.split("\\|");
			if (parts.length == 2) {
				return parts[1]; // Return the schema name
			}
		}
		// If no schema specified, return null to let OracleJdbcDdl.getSchema() use
		// getUserName()
		return null;
	}

}
