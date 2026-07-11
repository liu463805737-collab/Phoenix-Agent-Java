package com.phoenix.data.connector.accessor;

import com.phoenix.data.bo.DbConfigBO;
import com.phoenix.data.bo.schema.*;
import com.phoenix.data.connector.DbQueryParameter;
import com.phoenix.data.connector.SqlExecutor;
import com.phoenix.data.connector.ddl.AbstractJdbcDdl;
import com.phoenix.data.connector.ddl.DdlFactory;
import com.phoenix.data.connector.pool.DBConnectionPool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.util.List;

/**
 * 数据库访问器抽象基类，提供通用的数据库访问方法实现。
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */
@Slf4j
@AllArgsConstructor
public abstract class AbstractAccessor implements Accessor {

	private final DdlFactory ddlFactory;

	private final DBConnectionPool dbConnectionPool;

	/**
	 * 访问数据库并执行指定方法。
	 */
	public <T> T accessDb(DbConfigBO dbConfig, String method, DbQueryParameter param) throws Exception {

		try (Connection connection = getConnection(dbConfig)) {

			AbstractJdbcDdl ddlExecutor = (AbstractJdbcDdl) ddlFactory.getDdlExecutorByDbConfig(dbConfig);

			switch (method) {
				case "showDatabases":
					return (T) ddlExecutor.showDatabases(connection);
				case "showSchemas":
					return (T) ddlExecutor.showSchemas(connection);
				case "showTables":
					return (T) ddlExecutor.showTables(connection, param.getSchema(), param.getTablePattern());
				case "fetchTables":
					return (T) ddlExecutor.fetchTables(connection, param.getSchema(), param.getTables());
				case "showColumns":
					return (T) ddlExecutor.showColumns(connection, param.getSchema(), param.getTable());
				case "showForeignKeys":
					return (T) ddlExecutor.showForeignKeys(connection, param.getSchema(), param.getTables());
				case "sampleColumn":
					return (T) ddlExecutor.sampleColumn(connection, param.getSchema(), param.getTable(),
							param.getColumn());
				case "scanTable":
					return (T) ddlExecutor.scanTable(connection, param.getSchema(), param.getTable());
				case "executeSqlAndReturnObject":
					return (T) SqlExecutor.executeSqlAndReturnObject(connection, param.getSchema(), param.getSql());
				default:
					throw new UnsupportedOperationException("Unknown method: " + method);
			}
		}
		catch (Exception e) {

			log.error("Error accessing database with method: {}, reason: {}", method, e.getMessage());
			throw e;
		}
	}

	/**
	 * 获取数据库列表。
	 */
	public List<DatabaseInfoBO> showDatabases(DbConfigBO dbConfig) throws Exception {
		return accessDb(dbConfig, "showDatabases", null);
	}

	/**
	 * 获取模式列表。
	 */
	public List<SchemaInfoBO> showSchemas(DbConfigBO dbConfig) throws Exception {
		return accessDb(dbConfig, "showSchemas", null);
	}

	/**
	 * 获取表列表。
	 */
	public List<TableInfoBO> showTables(DbConfigBO dbConfig, DbQueryParameter param) throws Exception {
		return accessDb(dbConfig, "showTables", param);
	}

	/**
	 * 获取指定表的信息。
	 */
	public List<TableInfoBO> fetchTables(DbConfigBO dbConfig, DbQueryParameter param) throws Exception {
		return accessDb(dbConfig, "fetchTables", param);
	}

	/**
	 * 获取表的列信息。
	 */
	public List<ColumnInfoBO> showColumns(DbConfigBO dbConfig, DbQueryParameter param) throws Exception {
		return accessDb(dbConfig, "showColumns", param);
	}

	/**
	 * 获取表的外键信息。
	 */
	public List<ForeignKeyInfoBO> showForeignKeys(DbConfigBO dbConfig, DbQueryParameter param) throws Exception {
		return accessDb(dbConfig, "showForeignKeys", param);
	}

	/**
	 * 采样指定列的数据。
	 */
	public List<String> sampleColumn(DbConfigBO dbConfig, DbQueryParameter param) throws Exception {
		return accessDb(dbConfig, "sampleColumn", param);
	}

	/**
	 * 扫描表数据。
	 */
	public ResultSetBO scanTable(DbConfigBO dbConfig, DbQueryParameter param) throws Exception {
		return accessDb(dbConfig, "scanTable", param);
	}

	/**
	 * 执行 SQL 查询并返回结构化结果。
	 */
	public ResultSetBO executeSqlAndReturnObject(DbConfigBO dbConfig, DbQueryParameter param) throws Exception {
		return accessDb(dbConfig, "executeSqlAndReturnObject", param);
	}

	/**
	 * 获取数据库连接。
	 */
	public Connection getConnection(DbConfigBO config) {
		return this.dbConnectionPool.getConnection(config);
	}

}
