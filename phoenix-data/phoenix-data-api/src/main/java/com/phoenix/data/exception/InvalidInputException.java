package com.phoenix.data.exception;

import lombok.Getter;

/**
 * 无效输入异常
 */
public class InvalidInputException extends RuntimeException {

	@Getter
	private Object data;

	/**
	 * 构造无效输入异常
	 *
	 * @param message 异常信息
	 */
	public InvalidInputException(String message) {
		super(message);
	}

	/**
	 * 构造无效输入异常（携带额外数据）
	 *
	 * @param message 异常信息
	 * @param data 额外数据
	 */
	public InvalidInputException(String message, Object data) {
		super(message);
		this.data = data;
	}

}
