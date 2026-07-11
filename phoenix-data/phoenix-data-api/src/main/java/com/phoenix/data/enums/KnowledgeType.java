package com.phoenix.data.enums;

import lombok.Getter;

/**
 * 知识类型枚举
 */
@Getter
public enum KnowledgeType {

	/**
	 * 文档类型
	 */
	DOCUMENT("DOCUMENT", "文档类型"),

	/**
	 * 问答类型
	 */
	QA("QA", "问答类型"),

	/**
	 * 常见问题类型
	 */
	FAQ("FAQ", "常见问题类型");

	private final String code;

	private final String description;

	/**
	 * 构造知识类型枚举
	 *
	 * @param code 类型编码
	 * @param description 类型描述
	 */
	KnowledgeType(String code, String description) {
		this.code = code;
		this.description = description;
	}

	/**
	 * 根据代码获取枚举
	 */
	public static KnowledgeType fromCode(String code) {
		for (KnowledgeType type : values()) {
			// 严格比对
			if (type.getCode().equals(code)) {
				return type;
			}
		}
		throw new IllegalArgumentException("未知的知识类型代码: " + code);
	}

}
