package com.phoenix.data.enums;

import java.util.Optional;

/**
 * 数据库方言枚举
 */
public enum DatabaseDialectEnum {

	MYSQL("MySQL"),

	SQLite("SQLite"),

	POSTGRESQL("PostgreSQL"),

	H2("H2"),

	DAMENG("Dameng"),

	SQL_SERVER("SqlServer"),

	ORACLE("Oracle"),

	HIVE("Hive");

	public final String code;

	/**
	 * 构造数据库方言枚举
	 *
	 * @param code 方言编码
	 */
	DatabaseDialectEnum(String code) {
		this.code = code;
	}

	/**
	 * 获取方言编码
	 *
	 * @return 方言编码
	 */
	public String getCode() {
		return code;
	}

	/**
	 * 根据编码获取方言枚举
	 *
	 * @param code 方言编码
	 * @return 包含枚举的Optional，未找到返回empty
	 */
	public static Optional<DatabaseDialectEnum> getByCode(String code) {
		for (DatabaseDialectEnum value : values()) {
			if (value.code.equals(code)) {
				return Optional.of(value);
			}
		}
		return Optional.empty();
	}

}
