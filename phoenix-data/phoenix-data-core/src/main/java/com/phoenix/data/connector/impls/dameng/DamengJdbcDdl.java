package com.phoenix.data.connector.impls.dameng;

import com.phoenix.data.bo.schema.*;
import com.phoenix.data.connector.SqlExecutor;
import com.phoenix.data.connector.ddl.AbstractJdbcDdl;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.BooleanUtils;
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
 * 达梦数据库 JDBC DDL 执行器实现。
 */
@Service
public class DamengJdbcDdl extends AbstractJdbcDdl {

	/**
	 * 获取数据库列表（达梦通常以实例+用户作为 schema，此处返回空集）。
	 */
	@Override
	public List<DatabaseInfoBO> showDatabases(Connection connection) {
		// 达梦通常以实例+用户作为schema，数据库枚举意义不大，这里返回空集
		return Collections.emptyList();
	}

	/**
	 * 获取模式列表。
	 */
	@Override
	public List<SchemaInfoBO> showSchemas(Connection connection) {
		String sql = "SELECT USERNAME FROM SYS.ALL_USERS";
		List<SchemaInfoBO> schemaInfoList = Lists.newArrayList();
		try {
			String[][] resultArr = SqlExecutor.executeSqlAndReturnArr(connection, sql);
			if (resultArr.length <= 1) {
				return Lists.newArrayList();
			}
			for (int i = 1; i < resultArr.length; i++) {
				if (resultArr[i].length == 0) {
					continue;
				}
				String schema = resultArr[i][0];
				schemaInfoList.add(SchemaInfoBO.builder().name(schema).build());
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return schemaInfoList;
	}

	/**
	 * 获取表列表。
	 */
	@Override
	public List<TableInfoBO> showTables(Connection connection, String schema, String tablePattern) {
		String sql = "SELECT TABLE_NAME FROM USER_TABLES";
		if (StringUtils.isNotBlank(tablePattern)) {
			sql = "SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME LIKE '%' || '" + tablePattern + "' || '%'";
		}
		List<TableInfoBO> tableInfoList = Lists.newArrayList();
		try {
			String[][] resultArr = SqlExecutor.executeSqlAndReturnArr(connection, sql);
			if (resultArr.length <= 1) {
				return Lists.newArrayList();
			}
			for (int i = 1; i < resultArr.length; i++) {
				if (resultArr[i].length == 0) {
					continue;
				}
				String tableName = resultArr[i][0];
				tableInfoList.add(TableInfoBO.builder().name(tableName).description("").build());
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
		if (tables == null || tables.isEmpty()) {
			return Lists.newArrayList();
		}
		String tableListStr = String.join(", ", tables.stream().map(x -> "'" + x + "'").collect(Collectors.toList()));
		String sql = "SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME in(" + tableListStr + ")";
		List<TableInfoBO> tableInfoList = Lists.newArrayList();
		try {
			String[][] resultArr = SqlExecutor.executeSqlAndReturnArr(connection, sql);
			if (resultArr.length <= 1) {
				return Lists.newArrayList();
			}
			for (int i = 1; i < resultArr.length; i++) {
				if (resultArr[i].length == 0) {
					continue;
				}
				String tableName = resultArr[i][0];
				tableInfoList.add(TableInfoBO.builder().name(tableName).description("").build());
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return tableInfoList;
	}

	/**
	 * 获取表的列信息。
	 */
	@Override
	public List<ColumnInfoBO> showColumns(Connection connection, String schema, String table) {
		String sql = "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NULLABLE FROM USER_TAB_COLUMNS WHERE TABLE_NAME='"
				+ table + "'";
		List<ColumnInfoBO> columnInfoList = Lists.newArrayList();
		try {
			String[][] resultArr = SqlExecutor.executeSqlAndReturnArr(connection, null, sql);
			if (resultArr.length <= 1) {
				return Lists.newArrayList();
			}
			for (int i = 1; i < resultArr.length; i++) {
				if (resultArr[i].length == 0) {
					continue;
				}
				columnInfoList.add(ColumnInfoBO.builder()
					.name(resultArr[i][0])
					.description("")
					.type(wrapType(resultArr[i][1]))
					.primary(false)
					.notnull(BooleanUtils.toBoolean("N".equalsIgnoreCase(resultArr[i][3]) ? "true" : "false"))
					.build());
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return columnInfoList;
	}

	/**
	 * 获取表的外键信息。
	 */
	@Override
	public List<ForeignKeyInfoBO> showForeignKeys(Connection connection, String schema, List<String> tables) {
		if (tables == null || tables.isEmpty()) {
			return Lists.newArrayList();
		}
		String tableListStr = String.join(", ", tables.stream().map(x -> "'" + x + "'").collect(Collectors.toList()));
		String sql = "SELECT uc.TABLE_NAME, ucc.COLUMN_NAME, uc.CONSTRAINT_NAME, uc.R_OWNER, uc.R_CONSTRAINT_NAME "
				+ "FROM USER_CONSTRAINTS uc JOIN USER_CONS_COLUMNS ucc ON uc.CONSTRAINT_NAME = ucc.CONSTRAINT_NAME "
				+ "WHERE uc.CONSTRAINT_TYPE='R' AND uc.TABLE_NAME IN (" + tableListStr + ")";
		List<ForeignKeyInfoBO> foreignKeyInfoList = Lists.newArrayList();
		try {
			String[][] resultArr = SqlExecutor.executeSqlAndReturnArr(connection, null, sql);
			if (resultArr.length <= 1) {
				return Lists.newArrayList();
			}
			for (int i = 1; i < resultArr.length; i++) {
				if (resultArr[i].length == 0) {
					continue;
				}
				foreignKeyInfoList.add(ForeignKeyInfoBO.builder()
					.table(resultArr[i][0])
					.column(resultArr[i][1])
					.referencedTable("")
					.referencedColumn("")
					.build());
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return foreignKeyInfoList;
	}

	/**
	 * 采样指定列的数据。
	 */
	@Override
	public List<String> sampleColumn(Connection connection, String schema, String table, String column) {
		String sql = "SELECT " + column + " FROM " + table + " FETCH FIRST 99 ROWS ONLY";
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
			// ignore
		}
		Set<String> siSet = sampleInfo.stream().collect(Collectors.toSet());
		sampleInfo = siSet.stream().collect(Collectors.toList());
		return sampleInfo;
	}

	/**
	 * 扫描表数据。
	 */
	@Override
	public ResultSetBO scanTable(Connection connection, String schema, String table) {
		String sql = "SELECT * FROM " + table + " FETCH FIRST 20 ROWS ONLY";
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
		return BizDataSourceTypeEnum.DAMENG;
	}

}
