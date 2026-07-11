package com.phoenix.data.util;

import java.security.SecureRandom;

/**
 * Utility for generating and masking API keys.
 */
public final class ApiKeyUtil {

	private static final String API_KEY_PREFIX = "sk-";

	private static final String API_KEY_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	private static final int API_KEY_LENGTH = 32;

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	/**
	 * 私有构造方法，防止实例化
	 */
	private ApiKeyUtil() {
	}

	/**
	 * 生成随机API密钥
	 * @return 生成的API密钥
	 */
	public static String generate() {
		StringBuilder builder = new StringBuilder(API_KEY_PREFIX);
		for (int i = 0; i < API_KEY_LENGTH; i++) {
			int idx = SECURE_RANDOM.nextInt(API_KEY_CHARS.length());
			builder.append(API_KEY_CHARS.charAt(idx));
		}
		return builder.toString();
	}

	/**
	 * 对API密钥进行脱敏处理，只显示后4位
	 * @param apiKey 原始API密钥
	 * @return 脱敏后的密钥
	 */
	public static String mask(String apiKey) {
		if (apiKey == null || apiKey.length() <= 8) {
			return "****";
		}
		String suffix = apiKey.substring(apiKey.length() - 4);
		return "****" + suffix;
	}

}
