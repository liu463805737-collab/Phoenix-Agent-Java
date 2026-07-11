package com.phoenix.data.connector.impls.hive;

import com.phoenix.data.bo.schema.*;
import com.phoenix.data.connector.SqlExecutor;
import com.phoenix.data.connector.ddl.AbstractJdbcDdl;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.phoenix.data.util.ColumnTypeUtil.wrapType;

/**
 * Hive JDBC DDL 执行器实现
 */
@Service
public class HiveJdbcDdl extends AbstractJdbcDdl {

	/**
	 * 获取数据库列表。
	 */
	@Override
	public List<DatabaseInfoBO> showDatabases(Connection connection) {
		String sql = "SHOW DATABASES";
		List<DatabaseInfoBO> databaseInfoList = Lists.newArrayList();
		try {
			String[][] resultArr = SqlExecutor.executeSqlAndReturnArr(connection, sql);
			if (resultArr.length <= 1) {
				return Lists.newArrayList();
			}

			for (int i = 1; i < resultArr.length; i++) {
				if (resultArr[i].length == 0) {
					continue;
				}
				String database = resultArr[i][0];
				databaseInfoList.add(DatabaseInfoBO.builder().name(database).build());
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return databaseInfoList;
	}

	/**
	 * 获取模式列表（Hive 不区分 schema，返回空集）。
	 */
	@Override
	public List<SchemaInfoBO> showSchemas(Connection connection) {
		return Collections.emptyList();
	}

	/**
	 * 获取表列表。
	 */
	@Override
	public List<TableInfoBO> showTables(Connection connection, String schema, String tablePattern) {
		StringBuilder sql = new StringBuilder("SHOW TABLES");

		if (StringUtils.isNotBlank(schema)) {
			sql.append(" IN ").append(schema);
		}

		if (StringUtils.isNotBlank(tablePattern)) {
			sql.append(" LIKE '").append(tablePattern).append("'");
		}

		List<TableInfoBO> tableInfoList = Lists.newArrayList();
		try {
			String[][] resultArr = SqlExecutor.executeSqlAndReturnArr(connection, sql.toString());
			if (resultArr.length <= 1) {
				return Lists.newArrayList();
			}

			for (int i = 1; i < resultArr.length; i++) {
				if (resultArr[i].length == 0) {
					continue;
				}
				String tableName = resultArr[i][0];
				tableInfoList.add(TableInfoBO.builder().name(tableName).build());
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return tableInfoList;
	}

	/**
	 * 获取指定表的信息。
	 */
	@Override
	public List<TableInfoBO> fetchTables(Connection connection, String schema, List<String> tables) {
		List<TableInfoBO> tableInfoList = Lists.newArrayList();

		for (String tableName : tables) {
			try {
				String sql = "DESCRIBE FORMATTED " + (StringUtils.isNotBlank(schema) ? schema + "." : "") + tableName;
				String[][] resultArr = SqlExecutor.executeSqlAndReturnArr(connection, sql);

				String tableComment = "";
				for (int i = 1; i < resultArr.length; i++) {
					if (resultArr[i].length >= 2 && "comment".equalsIgnoreCase(resultArr[i][0].trim())) {
						tableComment = resultArr[i][1];
						break;
					}
				}

				tableInfoList.add(TableInfoBO.builder().name(tableName).description(tableComment).build());
			}
			catch (SQLException e) {
				tableInfoList.add(TableInfoBO.builder().name(tableName).build());
			}
		}

		return tableInfoList;
	}

	/**
	 * 获取表的列信息。
	 */
	@Override
	public List<ColumnInfoBO> showColumns(Connection connection, String schema, String table) {
		String fullTableName = StringUtils.isNotBlank(schema) ? schema + "." + table : table;
		String sql = "DESCRIBE " + fullTableName;

		List<ColumnInfoBO> columnInfoList = Lists.newArrayList();
		try {
			String[][] resultArr = SqlExecutor.executeSqlAndReturnArr(connection, sql);
			if (resultArr.length <= 1) {
				return Lists.newArrayList();
			}

			for (int i = 1; i < resultArr.length; i++) {
				if (resultArr[i].length < 2) {
					continue;
				}

				String colName = resultArr[i][0];
				String dataType = resultArr[i][1];
				String comment = resultArr[i].length >= 3 ? resultArr[i][2] : "";

				if (StringUtils.isBlank(colName) || colName.startsWith("#")) {
					continue;
				}

				columnInfoList.add(ColumnInfoBO.builder()
					.name(colName)
					.description(comment)
					.type(wrapType(dataType))
					.primary(false) // Hive 不支持主键
					.notnull(false) // Hive 不强制非空约束
					.build());
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return columnInfoList;
	}

	/**
	 * 获取表的外键信息（Hive 不支持外键，返回空集）。
	 */
	@Override
	public List<ForeignKeyInfoBO> showForeignKeys(Connection connection, String schema, List<String> tables) {
		return Collections.emptyList();
	}

	/**
	 * 采样指定列的数据。
	 */
	@Override
	public List<String> sampleColumn(Connection connection, String schema, String table, String column) {
		String fullTableName = StringUtils.isNotBlank(schema) ? schema + "." + table : table;
		String sql = String.format("SELECT `%s` FROM %s LIMIT 99", column, fullTableName);

		List<String> sampleInfo = Lists.newArrayList();
		try {
			String[][] resultArr = SqlExecutor.executeSqlAndReturnArr(connection, null, sql);
			if (resultArr.length <= 1) {
				return Lists.newArrayList();
			}

			for (int i = 1; i < resultArr.length; i++) {
				if (resultArr[i].length == 0 || column.equalsIgnoreCase(resultArr[i][0])) {
					continue;
				}
				sampleInfo.add(resultArr[i][0]);
			}
		}
		catch (SQLException e) {
		}

		// 去重
		Set<String> siSet = sampleInfo.stream().collect(Collectors.toSet());
		sampleInfo = siSet.stream().collect(Collectors.toList());
		return sampleInfo;
	}

	/**
	 * 扫描表数据。
	 */
	@Override
	public ResultSetBO scanTable(Connection connection, String schema, String table) {
		String fullTableName = StringUtils.isNotBlank(schema) ? schema + "." + table : table;
		String sql = String.format("SELECT * FROM %s LIMIT 20", fullTableName);

		ResultSetBO resultSet = ResultSetBO.builder().build();
		try {
			resultSet = SqlExecutor.executeSqlAndReturnObject(connection, schema, sql);
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return resultSet;
	}

	/**
	 * 获取数据源类型。
	 */
	@Override
	public BizDataSourceTypeEnum getDataSourceType() {
		return BizDataSourceTypeEnum.HIVE;
	}

}
