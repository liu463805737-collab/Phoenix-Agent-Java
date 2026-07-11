package com.phoenix.data.service.datasource.handler.impl;

import com.phoenix.data.entity.Datasource;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import com.phoenix.data.service.datasource.handler.DatasourceTypeHandler;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * MySQL 数据源类型处理器
 */
@Component
public class MysqlDatasourceTypeHandler implements DatasourceTypeHandler {

	/**
	 * 返回MySQL数据库类型名称
	 * @return 类型名称
	 */
	@Override
	public String typeName() {
		return BizDataSourceTypeEnum.MYSQL.getTypeName();
	}

	/**
	 * 构建MySQL JDBC连接URL
	 * @param datasource 数据源
	 * @return 连接URL
	 */
	@Override
	public String buildConnectionUrl(Datasource datasource) {
		if (!hasRequiredConnectionFields(datasource)) {
			return datasource.getConnectionUrl();
		}
		return String.format(
				"jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&transformedBitIsBoolean=true&allowMultiQueries=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Shanghai",
				datasource.getHost(), datasource.getPort(), datasource.getDatabaseName());
	}

	/**
	 * 规范化MySQL测试连接URL，补充必要参数
	 * @param datasource 数据源
	 * @param url 原始URL
	 * @return 规范化后的URL
	 */
	@Override
	public String normalizeTestUrl(Datasource datasource, String url) {
		String updated = url;
		String lowerUrl = updated.toLowerCase(Locale.ROOT);
		if (!lowerUrl.contains("servertimezone=")) {
			updated = appendParam(updated, "serverTimezone", "Asia/Shanghai");
			lowerUrl = updated.toLowerCase(Locale.ROOT);
		}
		if (!lowerUrl.contains("usessl=")) {
			updated = appendParam(updated, "useSSL", "false");
		}
		return updated;
	}

	/**
	 * 向URL追加查询参数
	 * @param url 原始URL
	 * @param key 参数名
	 * @param value 参数值
	 * @return 追加参数后的URL
	 */
	private String appendParam(String url, String key, String value) {
		return url + (url.contains("?") ? "&" : "?") + key + "=" + value;
	}

}
