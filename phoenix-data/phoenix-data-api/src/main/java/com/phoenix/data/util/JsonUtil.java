package com.phoenix.data.util;


import tools.jackson.databind.ObjectMapper;

/**
 * JSON工具类，提供ObjectMapper单例
 */
public class JsonUtil {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 获取全局ObjectMapper实例
	 * @return ObjectMapper对象
	 */
	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}

}
