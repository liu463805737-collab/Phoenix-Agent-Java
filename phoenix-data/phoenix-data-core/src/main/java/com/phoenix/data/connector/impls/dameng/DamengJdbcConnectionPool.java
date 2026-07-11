package com.phoenix.data.connector.impls.dameng;

import com.phoenix.data.connector.pool.AbstractDBConnectionPool;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import com.phoenix.data.enums.ErrorCodeEnum;
import org.springframework.stereotype.Service;

import static com.phoenix.data.enums.ErrorCodeEnum.OTHERS;

/**
 * 达梦数据库 JDBC 连接池实现。
 */
@Service("damengJdbcConnectionPool")
public class DamengJdbcConnectionPool extends AbstractDBConnectionPool {

	private static final String DRIVER = "dm.jdbc.driver.DmDriver";

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
		if (ret != null && ret != OTHERS) {
			return ret;
		}
		return OTHERS;
	}

	/**
	 * 判断是否支持指定的数据源类型。
	 */
	@Override
	public boolean supportedDataSourceType(String type) {
		return BizDataSourceTypeEnum.DAMENG.getTypeName().equals(type);
	}

	/**
	 * 获取连接池类型标识。
	 */
	@Override
	public String getConnectionPoolType() {
		return BizDataSourceTypeEnum.DAMENG.getTypeName();
	}

}
