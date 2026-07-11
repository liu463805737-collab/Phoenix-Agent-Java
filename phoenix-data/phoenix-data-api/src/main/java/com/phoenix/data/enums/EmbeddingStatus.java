package com.phoenix.data.enums;

import lombok.Getter;

/**
 * 向量化状态枚举
 */
@Getter
public enum EmbeddingStatus {

	PENDING("PENDING"), PROCESSING("PROCESSING"), COMPLETED("COMPLETED"), FAILED("FAILED");

	private final String value;

	/**
	 * 构造向量化状态枚举
	 *
	 * @param value 状态值
	 */
	EmbeddingStatus(String value) {
		this.value = value;
	}

	/**
	 * 根据值获取枚举实例
	 *
	 * @param value 状态值
	 * @return 枚举实例
	 * @throws IllegalArgumentException 未知的状态值
	 */
	public static EmbeddingStatus fromValue(String value) {
		for (EmbeddingStatus status : EmbeddingStatus.values()) {
			// 严格比对
			if (status.value.equals(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Unknown embedding status: " + value);
	}

}
