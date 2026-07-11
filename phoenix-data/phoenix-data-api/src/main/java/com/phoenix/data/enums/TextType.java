package com.phoenix.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文本类型枚举
 */
@AllArgsConstructor
@Getter
public enum TextType {

	JSON("$$$json", "$$$"),

	PYTHON("$$$python", "$$$"),

	// LLM模型爱输出```sql，那就换一个标记
	SQL("$$$sql", "$$$"),

	MARK_DOWN("$$$markdown-report", "$$$/markdown-report"),

	RESULT_SET("$$$result_set", "$$$"),

	TEXT(null, null);

	private final String startSign;

	private final String endSign;

	/**
	 * 根据文本块内容切换文本类型
	 *
	 * @param origin 当前文本类型
	 * @param chuck 文本块内容
	 * @return 切换后的文本类型
	 */
	public static TextType getType(TextType origin, String chuck) {
		if (origin == TEXT) {
			for (TextType type : TextType.values()) {
				if (chuck.equals(type.startSign)) {
					return type;
				}
			}
		}
		else {
			if (chuck.equals(origin.endSign)) {
				return TextType.TEXT;
			}
		}
		return origin;
	}

	/**
	 * 根据起始标记获取文本类型
	 *
	 * @param startSign 起始标记字符串
	 * @return 对应的文本类型，未找到返回TEXT
	 */
	public static TextType getTypeByStratSign(String startSign) {
		for (TextType type : TextType.values()) {
			if (startSign.equals(type.startSign)) {
				return type;
			}
		}
		return TextType.TEXT;
	}

}
