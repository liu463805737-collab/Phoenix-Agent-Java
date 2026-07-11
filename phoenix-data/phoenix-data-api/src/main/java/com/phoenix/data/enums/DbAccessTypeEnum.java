package com.phoenix.data.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * 数据库访问类型枚举
 */
public enum DbAccessTypeEnum {

	JDBC("jdbc"),

	SDK("sdk"),

	DATA_API("data-api"),

	FC_HTTP("fc-http"),

	MEMORY("in-memory");

	private String code;

	/**
	 * 构造数据库访问类型枚举
	 *
	 * @param code 访问类型编码
	 */
	DbAccessTypeEnum(String code) {
		this.code = code;
	}

	/**
	 * 根据编码获取枚举实例
	 *
	 * @param code 访问类型编码
	 * @return 枚举实例，无效编码返回null
	 */
	public static DbAccessTypeEnum of(String code) {
		if (StringUtils.isBlank(code)) {
			return null;
		}

		Optional<DbAccessTypeEnum> any = Arrays.stream(values())
			.filter(typeEnum -> code.equals(typeEnum.getCode()))
			.findAny();

		return any.orElse(null);
	}

	/**
	 * 获取访问类型编码
	 *
	 * @return 访问类型编码
	 */
	public String getCode() {
		return code;
	}

}
