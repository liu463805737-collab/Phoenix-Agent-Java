package com.phoenix.data.controller;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.phoenix.data.exception.InternalServerException;
import com.phoenix.data.exception.InvalidInputException;
import com.phoenix.data.vo.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器 (WebFlux 版本)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 处理无效输入异常，返回400错误
	 *
	 * @param e 无效输入异常
	 * @return 错误响应
	 */
	@ExceptionHandler(InvalidInputException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiResponse<Object> handleInvalidInputException(InvalidInputException e) {
		log.warn("Invalid input: {}", e.getMessage());
		return ApiResponse.error(e.getMessage(), e.getData());
	}

	/**
	 * 处理内部服务器异常，返回500错误
	 *
	 * @param e 内部服务器异常
	 * @return 错误响应
	 */
	@ExceptionHandler(InternalServerException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiResponse<Object> handleInternalServerException(InternalServerException e) {
		log.error("Internal server error: {}", e.getMessage(), e);
		return ApiResponse.error(e.getMessage());
	}

	@ExceptionHandler(NotLoginException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ApiResponse<Object> handleNotLoginException(NotLoginException e) {
		log.warn("未授权访问: {}", e.getMessage());
		return ApiResponse.error("未授权，请先登录");
	}

	@ExceptionHandler(NotPermissionException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ApiResponse<Object> handleNotPermissionException(NotPermissionException e) {
		log.warn("权限不足: {}", e.getMessage());
		return ApiResponse.error("权限不足");
	}

	@ExceptionHandler(NotRoleException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ApiResponse<Object> handleNotRoleException(NotRoleException e) {
		log.warn("角色不匹配: {}", e.getMessage());
		return ApiResponse.error("角色不匹配");
	}

	/**
	 * 处理通用异常，返回500错误
	 *
	 * @param e 通用异常
	 * @return 错误响应
	 */
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiResponse<Object> handleGenericException(Exception e) {
		log.error("Unexpected error: {}", e.getMessage(), e);
		return ApiResponse.error("服务器内部错误");
	}

}
