package com.phoenix.data.connector;

import com.phoenix.data.bo.schema.ResultSetBO;
import com.phoenix.data.enums.DatabaseDialectEnum;
import com.phoenix.data.util.ResultSetConvertUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.List;

/**
 * Responsible for executing SQL and returning structured results.
 */
public class SqlExecutor {

	/**
	 * 结果集行数限制。
	 */
	public static final Integer RESULT_SET_LIMIT = 1000;

	/**
	 * SQL 语句执行超时时间（秒）。
	 */
	public static final Integer STATEMENT_TIMEOUT = 30;

	/**
	 * Execute SQL query and return structured results (with column information)
	 * @param connection database connection
	 * @param sql SQL statement
	 * @return ResultSetBO structured result
	 * @throws SQLException SQL execution exception
	 */
	public static ResultSetBO executeSqlAndReturnObject(Connection connection, String schema, String sql)
			throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.setMaxRows(RESULT_SET_LIMIT);
			statement.setQueryTimeout(STATEMENT_TIMEOUT);

			DatabaseMetaData metaData = connection.getMetaData();
			String dialect = metaData.getDatabaseProductName();

			if (dialect.equals(DatabaseDialectEnum.POSTGRESQL.code)) {
				if (StringUtils.isNotEmpty(schema)) {
					statement.execute("set search_path = '" + schema + "';");
				}
			}
			else if (dialect.equals(DatabaseDialectEnum.H2.code)) {
				if (StringUtils.isNotEmpty(schema)) {
					statement.execute("use " + schema + ";");
				}
			}
			else if (dialect.equals(DatabaseDialectEnum.ORACLE.code)) {
				if (StringUtils.isNotEmpty(schema)) {
					statement.execute("ALTER SESSION SET CURRENT_SCHEMA = " + schema);
				}
			}

			try (ResultSet rs = statement.executeQuery(sql)) {
				return ResultSetBuilder.buildFrom(rs, schema);
			}
		}
	}

	/**
	 * Execute SQL query and return string two-dimensional array format result
	 * @param connection database connection
	 * @param sql SQL statement
	 * @return two-dimensional array result
	 * @throws SQLException SQL execution exception
	 */
	/**
	 * 执行 SQL 查询并返回字符串二维数组格式结果。
	 */
	public static String[][] executeSqlAndReturnArr(Connection connection, String sql) throws SQLException {
		List<String[]> list = executeQuery(connection, sql);
		return list.toArray(new String[0][]);
	}

	/**
	 * 执行 SQL 查询并返回字符串二维数组格式结果（指定数据库或模式）。
	 */
	public static String[][] executeSqlAndReturnArr(Connection connection, String databaseOrSchema, String sql)
			throws SQLException {
		List<String[]> list = executeQuery(connection, databaseOrSchema, sql);
		return list.toArray(new String[0][]);
	}

	/**
	 * 执行 SQL 查询并返回列表结果。
	 */
	private static List<String[]> executeQuery(Connection connection, String sql) throws SQLException {
		try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {

			return ResultSetConvertUtil.convert(rs);
		}
	}

	/**
	 * 执行 SQL 查询并返回列表结果（指定数据库或模式）。
	 */
	private static List<String[]> executeQuery(Connection connection, String databaseOrSchema, String sql)
			throws SQLException {
		String originalDb = connection.getCatalog();
		DatabaseMetaData metaData = connection.getMetaData();
		String dialect = metaData.getDatabaseProductName();

		try (Statement statement = connection.createStatement()) {

			if (dialect.equals(DatabaseDialectEnum.MYSQL.code)) {
				if (StringUtils.isNotEmpty(databaseOrSchema)) {
					statement.execute("use `" + databaseOrSchema + "`;");
				}
			}
			else if (dialect.equals(DatabaseDialectEnum.POSTGRESQL.code)) {
				if (StringUtils.isNotEmpty(databaseOrSchema)) {
					statement.execute("set search_path = '" + databaseOrSchema + "';");
				}
			}
			else if (dialect.equals(DatabaseDialectEnum.ORACLE.code)) {
				if (StringUtils.isNotEmpty(databaseOrSchema)) {
					statement.execute("ALTER SESSION SET CURRENT_SCHEMA = " + databaseOrSchema);
				}
			}

			ResultSet rs = statement.executeQuery(sql);

			List<String[]> result = ResultSetConvertUtil.convert(rs);

			if (StringUtils.isNotEmpty(databaseOrSchema) && dialect.equals(DatabaseDialectEnum.MYSQL.code)) {
				statement.execute("use `" + originalDb + "`;");
			}

			return result;
		}
	}

}
