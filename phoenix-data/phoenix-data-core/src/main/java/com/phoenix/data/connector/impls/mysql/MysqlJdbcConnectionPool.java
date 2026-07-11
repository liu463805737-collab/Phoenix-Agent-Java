package com.phoenix.data.connector.impls.mysql;

import com.phoenix.data.connector.pool.AbstractDBConnectionPool;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import com.phoenix.data.enums.ErrorCodeEnum;
import org.springframework.stereotype.Service;

import static com.phoenix.data.enums.ErrorCodeEnum.*;

/**
 * MySQL 数据库 JDBC 连接池实现。
 */
@Service("mysqlJdbcConnectionPool")
public class MysqlJdbcConnectionPool extends AbstractDBConnectionPool {

	private final static String DRIVER = "com.mysql.cj.jdbc.Driver";

	/**
	 * 获取 JDBC 驱动类名。
	 */
	@Override
	public String getDriver() {
		return DRIVER;
	}

	/**
	 * 错误状态码映射。
	 */
	@Override
	public ErrorCodeEnum errorMapping(String sqlState) {

		ErrorCodeEnum ret = ErrorCodeEnum.fromCode(sqlState);
		if (ret != null) {
			return ret;
		}

		return switch (sqlState) {
			case "08S01" -> DATASOURCE_CONNECTION_FAILURE_08S01;
			case "28000" -> PASSWORD_ERROR_28000;
			case "42000" -> DATABASE_NOT_EXIST_42000;
			default -> OTHERS;
		};
	}

	/**
	 * 判断是否支持指定的数据源类型。
	 */
	@Override
	public boolean supportedDataSourceType(String type) {
		return BizDataSourceTypeEnum.MYSQL.getTypeName().equals(type);
	}

	/**
	 * 获取连接池类型标识。
	 */
	@Override
	public String getConnectionPoolType() {
		return BizDataSourceTypeEnum.MYSQL.getTypeName();
	}

}
