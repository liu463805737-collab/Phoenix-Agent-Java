package com.phoenix.data.enums;

/**
 * 业务数据源类型枚举
 */
public enum BizDataSourceTypeEnum {

	MYSQL(1, "mysql", DatabaseDialectEnum.MYSQL.getCode(), DbAccessTypeEnum.JDBC.getCode()),

	POSTGRESQL(2, "postgresql", DatabaseDialectEnum.POSTGRESQL.getCode(), DbAccessTypeEnum.JDBC.getCode()),

	SQLITE(3, "sqlite", DatabaseDialectEnum.MYSQL.getCode(), DbAccessTypeEnum.JDBC.getCode()),

	H2(4, "h2", DatabaseDialectEnum.H2.getCode(), DbAccessTypeEnum.JDBC.getCode()),

	/**
	 * Dameng database (达梦)
	 */
	DAMENG(5, "dameng", DatabaseDialectEnum.DAMENG.getCode(), DbAccessTypeEnum.JDBC.getCode()),

	/**
	 * SQL Server database
	 */
	SQL_SERVER(6, "sqlserver", DatabaseDialectEnum.SQL_SERVER.getCode(), DbAccessTypeEnum.JDBC.getCode()),

	/**
	 * Oracle database
	 */
	ORACLE(7, "oracle", DatabaseDialectEnum.ORACLE.getCode(), DbAccessTypeEnum.JDBC.getCode()),

	/**
	 * Hive database
	 */
	HIVE(8, "hive", DatabaseDialectEnum.HIVE.getCode(), DbAccessTypeEnum.JDBC.getCode()),

	HOLOGRESS(10, "hologress", DatabaseDialectEnum.POSTGRESQL.getCode(), DbAccessTypeEnum.JDBC.getCode()),

	MYSQL_VPC(11, "mysql-vpc", DatabaseDialectEnum.MYSQL.getCode(), DbAccessTypeEnum.JDBC.getCode()),

	POSTGRESQL_VPC(12, "postgresql-vpc", DatabaseDialectEnum.POSTGRESQL.getCode(), DbAccessTypeEnum.JDBC.getCode()),

	ADB_PG(21, "adg_pg", DatabaseDialectEnum.POSTGRESQL.getCode(), DbAccessTypeEnum.DATA_API.getCode()),

	MAX_COMPUTE(31, "max_compute", DatabaseDialectEnum.MYSQL.getCode(), DbAccessTypeEnum.JDBC.getCode()),
	// SQLite simulated database in function computation
	FC_MEMORY_DB(41, "fc_memory_db", DatabaseDialectEnum.SQLite.getCode(), DbAccessTypeEnum.FC_HTTP.getCode()),

	MYSQL_VIRTUAL(51, "mysql-virtual", DatabaseDialectEnum.MYSQL.getCode(), DbAccessTypeEnum.MEMORY.getCode()),

	POSTGRESQL_VIRTUAL(52, "postgresql-virtual", DatabaseDialectEnum.POSTGRESQL.getCode(),
			DbAccessTypeEnum.MEMORY.getCode());

	public final Integer code;

	public final String typeName;

	public final String dialect;

	public final String protocol;

	/**
	 * 构造业务数据源类型枚举
	 *
	 * @param code 类型编码
	 * @param typeName 类型名称
	 * @param dialect 数据库方言
	 * @param protocol 访问协议
	 */
	BizDataSourceTypeEnum(Integer code, String typeName, String dialect, String protocol) {
		this.code = code;
		this.typeName = typeName;
		this.dialect = dialect;
		this.protocol = protocol;
	}

	/**
	 * 获取类型编码
	 *
	 * @return 类型编码
	 */
	public Integer getCode() {
		return code;
	}

	/**
	 * 获取类型名称
	 *
	 * @return 类型名称
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * 获取访问协议
	 *
	 * @return 访问协议
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * 获取数据库方言
	 *
	 * @return 数据库方言
	 */
	public String getDialect() {
		return dialect;
	}

	/**
	 * Get corresponding typeName based on code.
	 * @param code code for which to get typeName
	 * @return corresponding typeName, return null if not found.
	 */
	public static String getTypeNameByCode(Integer code) {
		for (BizDataSourceTypeEnum type : values()) {
			if (type.getCode().equals(code)) {
				return type.getTypeName();
			}
		}
		return null; // If corresponding code is not found, return null
	}

	/**
	 * 根据编码获取数据库方言
	 *
	 * @param code 类型编码
	 * @return 数据库方言，未找到返回null
	 */
	public static String getDialectByCode(Integer code) {
		for (BizDataSourceTypeEnum type : values()) {
			if (type.getCode().equals(code)) {
				return type.getDialect();
			}
		}
		return null; // If corresponding code is not found, return null
	}

	/**
	 * 根据编码获取访问协议
	 *
	 * @param code 类型编码
	 * @return 访问协议，未找到返回null
	 */
	public static String getProtocolByCode(Integer code) {
		for (BizDataSourceTypeEnum type : values()) {
			if (type.getCode().equals(code)) {
				return type.getProtocol();
			}
		}
		return null;
	}

	/**
	 * 根据编码获取枚举实例
	 *
	 * @param code 类型编码
	 * @return 枚举实例，未找到返回null
	 */
	public static BizDataSourceTypeEnum fromCode(Integer code) {
		for (BizDataSourceTypeEnum type : values()) {
			if (type.getCode() == code) {
				return type;
			}
		}
		return null;
	}

	/**
	 * 根据类型名称获取枚举实例
	 *
	 * @param typeName 类型名称
	 * @return 枚举实例，未找到返回null
	 */
	public static BizDataSourceTypeEnum fromTypeName(String typeName) {
		for (BizDataSourceTypeEnum type : values()) {
			if (type.getTypeName().equals(typeName)) {
				return type;
			}
		}
		return null;
	}

	/**
	 * 判断是否为MySQL方言
	 *
	 * @param typeName 类型名称
	 * @return 是否为MySQL方言
	 */
	public static boolean isMysqlDialect(String typeName) {
		return isDialect(typeName, DatabaseDialectEnum.MYSQL.getCode());
	}

	/**
	 * 判断是否为SQL Server方言
	 *
	 * @param typeName 类型名称
	 * @return 是否为SQL Server方言
	 */
	public static boolean isSqlServerDialect(String typeName) {
		return isDialect(typeName, DatabaseDialectEnum.SQL_SERVER.getCode());
	}

	/**
	 * 判断是否为PostgreSQL方言
	 *
	 * @param typeName 类型名称
	 * @return 是否为PostgreSQL方言
	 */
	public static boolean isPgDialect(String typeName) {
		return isDialect(typeName, DatabaseDialectEnum.POSTGRESQL.getCode());
	}

	/**
	 * 判断是否为Oracle方言
	 *
	 * @param typeName 类型名称
	 * @return 是否为Oracle方言
	 */
	public static boolean isOracleDialect(String typeName) {
		return isDialect(typeName, DatabaseDialectEnum.ORACLE.getCode());
	}

	/**
	 * 判断是否为AnalyticDB for PostgreSQL类型
	 *
	 * @param typeName 类型名称
	 * @return 是否为ADB PG类型
	 */
	public static boolean isAdbPg(String typeName) {
		BizDataSourceTypeEnum te = fromTypeName(typeName);
		if (te == null) {
			return false;
		}
		if (DatabaseDialectEnum.POSTGRESQL.getCode().equalsIgnoreCase(te.getDialect())
				&& DbAccessTypeEnum.DATA_API.getCode().equalsIgnoreCase(te.getProtocol())) {
			return true;
		}
		return false;
	}

	/**
	 * 判断指定类型是否为指定方言
	 *
	 * @param typeName 类型名称
	 * @param dialect 方言名称
	 * @return 是否匹配指定方言
	 */
	public static boolean isDialect(String typeName, String dialect) {
		BizDataSourceTypeEnum te = fromTypeName(typeName);
		if (te == null) {
			return false;
		}
		if (dialect.equalsIgnoreCase(te.getDialect())) {
			return true;
		}
		return false;
	}

}
