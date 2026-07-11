package com.phoenix.data.exception;

/**
 * 内部服务异常
 */
public class InternalServerException extends RuntimeException {

	/**
	 * 构造内部服务异常
	 *
	 * @param message 异常信息
	 */
	public InternalServerException(String message) {
		super(message);
	}

}
