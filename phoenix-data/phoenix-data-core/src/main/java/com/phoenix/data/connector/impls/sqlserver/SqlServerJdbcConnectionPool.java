package com.phoenix.data.connector.impls.sqlserver;

import com.phoenix.data.connector.pool.AbstractDBConnectionPool;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import com.phoenix.data.enums.ErrorCodeEnum;
import org.springframework.stereotype.Service;

import static com.phoenix.data.enums.ErrorCodeEnum.*;

/**
 * SQL Server 数据库 JDBC 连接池实现。
 *
 * @author zihen
 * @date 2025/12/14 17:34
 */
@Service("sqlServerJdbcConnectionPool")
public class SqlServerJdbcConnectionPool extends AbstractDBConnectionPool {

	/**
	 * 获取 JDBC 驱动类名。
	 */
	@Override
	public String getDriver() {
		return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
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
			case "S0001" -> DATABASE_NOT_EXIST_42000;
			case "42000" -> DATABASE_NOT_EXIST_42000;
			default -> OTHERS;
		};
	}

	/**
	 * 判断是否支持指定的数据源类型。
	 */
	@Override
	public boolean supportedDataSourceType(String type) {
		return BizDataSourceTypeEnum.SQL_SERVER.getTypeName().equalsIgnoreCase(type);
	}

	/**
	 * 获取连接池类型标识。
	 */
	@Override
	public String getConnectionPoolType() {
		return BizDataSourceTypeEnum.SQL_SERVER.getTypeName();
	}

}
