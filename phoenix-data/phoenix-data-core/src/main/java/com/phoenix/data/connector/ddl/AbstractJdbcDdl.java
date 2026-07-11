package com.phoenix.data.connector.ddl;

import com.phoenix.data.bo.schema.*;
import com.phoenix.data.util.SqlUtil;

import java.sql.Connection;
import java.util.List;

/**
 * JDBC DDL 执行器抽象基类，提供 DDL 操作的通用方法定义。
 */
public abstract class AbstractJdbcDdl implements Ddl {

	/**
	 * 获取数据库列表（已废弃）。
	 */
	@Deprecated
	public abstract List<DatabaseInfoBO> showDatabases(Connection connection);

	/**
	 * 获取模式列表。
	 */
	public abstract List<SchemaInfoBO> showSchemas(Connection connection);

	/**
	 * 获取表列表。
	 */
	public abstract List<TableInfoBO> showTables(Connection connection, String schema, String tablePattern);

	/**
	 * 获取指定表的信息。
	 */
	public abstract List<TableInfoBO> fetchTables(Connection connection, String schema, List<String> tables);

	/**
	 * 获取表的列信息。
	 */
	public abstract List<ColumnInfoBO> showColumns(Connection connection, String schema, String table);

	/**
	 * 获取表的外键信息。
	 */
	public abstract List<ForeignKeyInfoBO> showForeignKeys(Connection connection, String schema, List<String> tables);

	/**
	 * 采样指定列的数据。
	 */
	public abstract List<String> sampleColumn(Connection connection, String schema, String table, String column);

	/**
	 * 扫描表数据。
	 */
	public abstract ResultSetBO scanTable(Connection connection, String schema, String table);

	/**
	 * 获取查询 SQL 语句。
	 */
	public String getSelectSql(String typeName, String tableName, String columnNames, int limit) {
		return SqlUtil.buildSelectSql(typeName, tableName, columnNames, limit);
	}

}
