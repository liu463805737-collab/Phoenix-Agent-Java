package com.phoenix.data.connector.pool;

import com.phoenix.data.bo.DbConfigBO;
import com.phoenix.data.enums.ErrorCodeEnum;

import java.sql.Connection;

/**
 * 数据库连接池接口，用于维护数据源连接信息。
 */

public interface DBConnectionPool extends AutoCloseable {

	/**
	 * 检测数据库连接是否可用。
	 */
	ErrorCodeEnum ping(DbConfigBO config);

	/**
	 * 从连接池中获取数据库连接。
	 */
	Connection getConnection(DbConfigBO config);

	/**
	 * 判断是否支持指定的数据源类型。
	 */
	boolean supportedDataSourceType(String type);

	/**
	 * 获取连接池类型标识。
	 */
	String getConnectionPoolType();

}
