package com.phoenix.data.dto.datasource;

/**
 * SQL 重试 DTO
 */
public record SqlRetryDto(String reason, boolean semanticFail, boolean sqlExecuteFail) {

	/**
	 * 创建语义失败的重试对象
	 */
	public static SqlRetryDto semantic(String reason) {
		return new SqlRetryDto(reason, true, false);
	}

	/**
	 * 创建 SQL 执行失败的重试对象
	 */
	public static SqlRetryDto sqlExecute(String reason) {
		return new SqlRetryDto(reason, false, true);
	}

	/**
	 * 创建空的重试对象（无需重试）
	 */
	public static SqlRetryDto empty() {
		return new SqlRetryDto("", false, false);
	}

}
