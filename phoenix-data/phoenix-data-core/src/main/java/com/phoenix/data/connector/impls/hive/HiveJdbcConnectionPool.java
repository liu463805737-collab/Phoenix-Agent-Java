package com.phoenix.data.connector.impls.hive;

import com.phoenix.data.bo.DbConfigBO;
import com.phoenix.data.connector.pool.AbstractDBConnectionPool;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import com.phoenix.data.enums.ErrorCodeEnum;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.phoenix.data.enums.ErrorCodeEnum.*;

/**
 * Hive JDBC connection pool implementation.
 */
@Slf4j
@Service("hiveJdbcConnectionPool")
public class HiveJdbcConnectionPool extends AbstractDBConnectionPool {

	private static final String DRIVER = "org.apache.hive.jdbc.HiveDriver";

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
		if (sqlState == null) {
			return OTHERS;
		}

		ErrorCodeEnum ret = ErrorCodeEnum.fromCode(sqlState);
		if (ret != OTHERS) {
			return ret;
		}

		switch (sqlState) {
			case "08001":
			case "08S01":
				return DATASOURCE_CONNECTION_FAILURE_08001;
			case "28000":
				return PASSWORD_ERROR_28000;
			case "42000":
				return DATABASE_NOT_EXIST_42000;
			case "42501":
				return INSUFFICIENT_PRIVILEGE_42501;
			default:
				return OTHERS;
		}
	}

	/**
	 * 判断是否支持指定的数据源类型。
	 */
	@Override
	public boolean supportedDataSourceType(String type) {
		return BizDataSourceTypeEnum.HIVE.getTypeName().equals(type);
	}

	/**
	 * 获取连接池类型标识。
	 */
	@Override
	public String getConnectionPoolType() {
		return "Hive_JDBC_Pool";
	}

	/**
	 * 创建 Hive 数据源。
	 */
	@Override
	public DataSource createdDataSource(String url, String username, String password) throws Exception {
		log.info("Creating Hive DataSource with custom configuration");
		String driver = getDriver();
		Map<String, String> props = new HiveDruidProperties(driver, url, username, password, "stat").toMap();
		return DruidDataSourceFactory.createDataSource(props);
	}

	/**
	 * Hive Druid 连接池属性配置类。
	 */
	private static final class HiveDruidProperties {

		private final String driver;

		private final String url;

		private final String username;

		private final String password;

		private final String filters;

		/**
		 * 构造函数。
		 */
		private HiveDruidProperties(String driver, String url, String username, String password, String filters) {
			this.driver = driver;
			this.url = url;
			this.username = username;
			this.password = password;
			this.filters = filters;
		}

		/**
		 * 转换为 Druid 连接池属性 Map。
		 */
		private Map<String, String> toMap() {
			Map<String, String> props = new HashMap<>();
			props.put(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, this.driver);
			props.put(DruidDataSourceFactory.PROP_URL, this.url);
			props.put(DruidDataSourceFactory.PROP_USERNAME, this.username);
			props.put(DruidDataSourceFactory.PROP_PASSWORD, this.password);
			props.put(DruidDataSourceFactory.PROP_FILTERS, this.filters);
			props.put(DruidDataSourceFactory.PROP_INITIALSIZE, "5");
			props.put(DruidDataSourceFactory.PROP_MINIDLE, "5");
			props.put(DruidDataSourceFactory.PROP_MAXACTIVE, "20");
			props.put(DruidDataSourceFactory.PROP_MAXWAIT, "60000");
			props.put(DruidDataSourceFactory.PROP_TIMEBETWEENEVICTIONRUNSMILLIS, "60000");
			props.put(DruidDataSourceFactory.PROP_MINEVICTABLEIDLETIMEMILLIS, "300000");
			props.put(DruidDataSourceFactory.PROP_VALIDATIONQUERY, "SELECT 1");
			props.put(DruidDataSourceFactory.PROP_TESTWHILEIDLE, "true");
			props.put(DruidDataSourceFactory.PROP_TESTONBORROW, "false");
			props.put(DruidDataSourceFactory.PROP_TESTONRETURN, "false");
			return props;
		}

	}

	/**
	 * 检测 Hive 数据库连接是否可用。
	 */
	@Override
	public ErrorCodeEnum ping(DbConfigBO config) {
		log.info("Hive ping method called, url: {}", config.getUrl());
		try (Connection connection = getConnection(config); Statement stmt = connection.createStatement()) {
			log.info("Hive connection obtained, executing SELECT 1");
			ResultSet rs = stmt.executeQuery("SELECT 1");
			if (rs.next()) {
				rs.close();
				return SUCCESS;
			}
			rs.close();
			return DATASOURCE_CONNECTION_FAILURE_08001;
		}
		catch (SQLException e) {
			log.error("Hive connection test failed, url:{}, state:{}, message:{}", config.getUrl(), e.getSQLState(),
					e.getMessage());
			return errorMapping(e.getSQLState());
		}
		catch (Exception e) {
			log.error("Hive connection test failed with unexpected error, url:{}, message:{}", config.getUrl(),
					e.getMessage());
			return DATASOURCE_CONNECTION_FAILURE_08001;
		}
	}

}
