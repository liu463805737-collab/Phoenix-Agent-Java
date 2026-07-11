package com.phoenix.data.util;

/**
 * 数据库列类型转换工具类
 */
public class ColumnTypeUtil {

	/**
	 * 将数据库列类型转换为通用类型（number/text）
	 * @param s 数据库列类型
	 * @return 转换后的通用类型
	 */
	public static String wrapType(String s) {
		if (s.equalsIgnoreCase("decimal") || s.equalsIgnoreCase("int") || s.equalsIgnoreCase("bigint")
				|| s.equalsIgnoreCase("bool") || s.equalsIgnoreCase("bit") || s.equalsIgnoreCase("boolean")
				|| s.equalsIgnoreCase("double")) {
			return "number";
		}
		else if (s.startsWith("varchar") || s.startsWith("char")) {
			return "text";
		}
		return s;
	}

}
