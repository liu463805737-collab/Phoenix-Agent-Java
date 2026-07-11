package com.phoenix.data.enums;

import lombok.Getter;

/**
 * 文本分块策略枚举
 */
@Getter
public enum SplitterType {

	TOKEN("token"), RECURSIVE("recursive"), SENTENCE("sentence"), PARAGRAPH("paragraph"), SEMANTIC("semantic");

	private final String value;

	/**
	 * 构造分块策略枚举
	 *
	 * @param value 策略值
	 */
	SplitterType(String value) {
		this.value = value;
	}

}
