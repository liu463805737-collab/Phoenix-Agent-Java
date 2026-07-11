package com.phoenix.data.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 分页响应类
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PageResponse<T> extends ApiResponse<T> {

	/**
	 * 总记录数
	 */
	private Long total;

	/**
	 * 当前页码
	 */
	private Integer pageNum;

	/**
	 * 每页大小
	 */
	private Integer pageSize;

	/**
	 * 总页数
	 */
	private Integer totalPages;

	/**
	 * 构造方法
	 * @param success 是否成功
	 * @param message 消息
	 */
	public PageResponse(boolean success, String message) {
		super(success, message);
	}

	@SuppressWarnings("unchecked")
	/**
	 * 构造方法
	 * @param success 是否成功
	 * @param message 消息
	 * @param data 数据
	 */
	public PageResponse(boolean success, String message, T data) {
		super(success, message, data);
	}

	@SuppressWarnings("unchecked")
	/**
	 * 全参构造方法
	 * @param success 是否成功
	 * @param message 消息
	 * @param data 数据
	 * @param total 总记录数
	 * @param pageNum 当前页码
	 * @param pageSize 每页大小
	 * @param totalPages 总页数
	 */
	public PageResponse(boolean success, String message, T data, Long total, Integer pageNum, Integer pageSize,
			Integer totalPages) {
		super(success, message, data);
		this.total = total;
		this.pageNum = pageNum;
		this.pageSize = pageSize;
		this.totalPages = totalPages;
	}

	/**
	 * 创建成功分页响应
	 * @param data 数据
	 * @param total 总记录数
	 * @param pageNum 当前页码
	 * @param pageSize 每页大小
	 * @param totalPages 总页数
	 * @return PageResponse实例
	 */
	public static <T> PageResponse<T> success(T data, Long total, Integer pageNum, Integer pageSize,
			Integer totalPages) {
		return new PageResponse<>(true, "查询成功", data, total, pageNum, pageSize, totalPages);
	}

	/**
	 * 创建带自定义消息的成功分页响应
	 * @param message 成功消息
	 * @param data 数据
	 * @param total 总记录数
	 * @param pageNum 当前页码
	 * @param pageSize 每页大小
	 * @param totalPages 总页数
	 * @return PageResponse实例
	 */
	public static <T> PageResponse<T> success(String message, T data, Long total, Integer pageNum, Integer pageSize,
			Integer totalPages) {
		return new PageResponse<>(true, message, data, total, pageNum, pageSize, totalPages);
	}

	/**
	 * 创建分页错误响应
	 * @param message 错误消息
	 * @return PageResponse实例
	 */
	public static <T> PageResponse<T> pageError(String message) {
		return new PageResponse<>(false, message);
	}

	/**
	 * 创建带数据的分页错误响应
	 * @param message 错误消息
	 * @param data 数据
	 * @return PageResponse实例
	 */
	public static <T> PageResponse<T> pageError(String message, T data) {
		return new PageResponse<>(false, message, data);
	}

}
