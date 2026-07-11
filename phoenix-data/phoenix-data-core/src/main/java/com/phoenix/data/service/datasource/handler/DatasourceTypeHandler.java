package com.phoenix.data.service.datasource.handler;

import com.phoenix.data.bo.DbConfigBO;
import com.phoenix.data.entity.Datasource;
import com.phoenix.data.enums.DbAccessTypeEnum;
import org.springframework.util.StringUtils;

/**
 * 数据源类型处理器接口，定义不同类型数据源的连接参数构建、方言类型等行为
 */
public interface DatasourceTypeHandler {

	/**
	 * 获取数据源类型名称
	 * @return 类型名称
	 */
	String typeName();

	/**
	 * 获取连接类型，默认为 JDBC
	 * @return 连接类型
	 */
	default String connectionType() {
		return DbAccessTypeEnum.JDBC.getCode();
	}

	/**
	 * 获取数据库方言类型，默认为数据源类型名称
	 * @return 方言类型
	 */
	default String dialectType() {
		return typeName();
	}

	/**
	 * 判断当前处理器是否支持指定的数据源类型
	 * @param type 数据源类型
	 * @return 是否支持
	 */
	default boolean supports(String type) {
		return typeName().equalsIgnoreCase(type);
	}

	/**
	 * 检查数据源是否包含必需的连接字段
	 * @param datasource 数据源
	 * @return 是否包含必需字段
	 */
	default boolean hasRequiredConnectionFields(Datasource datasource) {
		return datasource.getHost() != null && datasource.getPort() != null && datasource.getDatabaseName() != null;
	}

	/**
	 * 构建数据源连接URL
	 * @param datasource 数据源
	 * @return 连接URL
	 */
	default String buildConnectionUrl(Datasource datasource) {
		return datasource.getConnectionUrl();
	}

	/**
	 * 解析连接URL，优先使用已有的URL，否则重新构建
	 * @param datasource 数据源
	 * @return 连接URL
	 */
	default String resolveConnectionUrl(Datasource datasource) {
		String existing = datasource.getConnectionUrl();
		if (StringUtils.hasText(existing)) {
			return existing;
		}
		return buildConnectionUrl(datasource);
	}

	/**
	 * 提取数据源的Schema名称
	 * @param datasource 数据源
	 * @return Schema名称
	 */
	default String extractSchemaName(Datasource datasource) {
		return datasource.getDatabaseName();
	}

	/**
	 * 将数据源实体转换为数据库配置对象
	 * @param datasource 数据源
	 * @return 数据库配置
	 */
	default DbConfigBO toDbConfig(Datasource datasource) {
		DbConfigBO config = new DbConfigBO();
		config.setUrl(resolveConnectionUrl(datasource));
		config.setUsername(datasource.getUsername());
		config.setPassword(datasource.getPassword());
		config.setConnectionType(connectionType());
		config.setDialectType(dialectType());
		config.setSchema(extractSchemaName(datasource));
		return config;
	}

	/**
	 * 规范化测试连接URL
	 * @param datasource 数据源
	 * @param url 原始URL
	 * @return 规范化后的URL
	 */
	default String normalizeTestUrl(Datasource datasource, String url) {
		return url;
	}

}
