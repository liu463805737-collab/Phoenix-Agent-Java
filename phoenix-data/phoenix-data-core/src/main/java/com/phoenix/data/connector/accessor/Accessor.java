package com.phoenix.data.connector.accessor;

import com.phoenix.data.bo.DbConfigBO;
import com.phoenix.data.bo.schema.*;
import com.phoenix.data.connector.DbQueryParameter;
import com.phoenix.data.enums.BizDataSourceTypeEnum;

import java.util.List;

/**
 * Data access interface definition.
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

/**
 * 数据库访问器接口定义。
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */
public interface Accessor {

	/**
	 * 获取访问器类型标识。
	 */
	String getAccessorType();

	/**
	 * 判断是否支持指定的数据源类型。
	 */
	boolean supportedDataSourceType(String type);

	default boolean supportedDataSourceType(BizDataSourceTypeEnum typeEnum) {
		return supportedDataSourceType(typeEnum.getTypeName());
	}

	/**
	 * Access the database and execute the specified method with the given parameters.
	 * @param dbConfig database configuration
	 * @param method method name
	 * @param param query parameters
	 * @return result object, which can be a list of database information, schema
	 * information, table information, etc.
	 * @throws Exception if an error occurs during database access
	 */
	<T> T accessDb(DbConfigBO dbConfig, String method, DbQueryParameter param) throws Exception;

	/**
	 * 获取数据库列表。
	 */
	List<DatabaseInfoBO> showDatabases(DbConfigBO dbConfig) throws Exception;

	/**
	 * 获取模式列表。
	 */
	List<SchemaInfoBO> showSchemas(DbConfigBO dbConfig) throws Exception;

	/**
	 * 获取表列表。
	 */
	List<TableInfoBO> showTables(DbConfigBO dbConfig, DbQueryParameter param) throws Exception;

	/**
	 * 获取指定表的信息。
	 */
	List<TableInfoBO> fetchTables(DbConfigBO dbConfig, DbQueryParameter param) throws Exception;

	/**
	 * 获取表的列信息。
	 */
	List<ColumnInfoBO> showColumns(DbConfigBO dbConfig, DbQueryParameter param) throws Exception;

	/**
	 * 获取表的外键信息。
	 */
	List<ForeignKeyInfoBO> showForeignKeys(DbConfigBO dbConfig, DbQueryParameter param) throws Exception;

	/**
	 * 采样指定列的数据。
	 */
	List<String> sampleColumn(DbConfigBO dbConfig, DbQueryParameter param) throws Exception;

	/**
	 * 扫描表数据。
	 */
	ResultSetBO scanTable(DbConfigBO dbConfig, DbQueryParameter param) throws Exception;

	/**
	 * 执行 SQL 查询并返回结构化结果。
	 */
	ResultSetBO executeSqlAndReturnObject(DbConfigBO dbConfig, DbQueryParameter param) throws Exception;

}
