package com.phoenix.data.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Common API Response Class
 */
@Data
@NoArgsConstructor
public class ApiResponse<T> {

	private boolean success;

	private String message;

	private T data;

	/**
	 * 构造方法
	 * @param success 是否成功
	 * @param message 消息
	 */
	public ApiResponse(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	/**
	 * 构造方法
	 * @param success 是否成功
	 * @param message 消息
	 * @param data 数据
	 */
	public ApiResponse(boolean success, String message, T data) {
		this.success = success;
		this.message = message;
		this.data = data;
	}

	/**
	 * 创建成功响应
	 * @param message 成功消息
	 * @return ApiResponse实例
	 */
	public static <T> ApiResponse<T> success(String message) {
		return new ApiResponse<>(true, message);
	}

	/**
	 * 创建带数据的成功响应
	 * @param message 成功消息
	 * @param data 响应数据
	 * @return ApiResponse实例
	 */
	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(true, message, data);
	}

	/**
	 * 创建错误响应
	 * @param message 错误消息
	 * @return ApiResponse实例
	 */
	public static <T> ApiResponse<T> error(String message) {
		return new ApiResponse<>(false, message);
	}

	/**
	 * 创建带数据的错误响应
	 * @param message 错误消息
	 * @param data 响应数据
	 * @return ApiResponse实例
	 */
	public static <T> ApiResponse<T> error(String message, T data) {
		return new ApiResponse<>(false, message, data);
	}

}
