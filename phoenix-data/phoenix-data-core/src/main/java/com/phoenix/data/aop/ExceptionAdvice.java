package com.phoenix.data.aop;

import com.phoenix.data.vo.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理切面，统一处理 Controller 层抛出的异常并返回标准错误响应。
 */
@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

	/**
	 * 处理所有未捕获的异常，记录错误日志并返回 500 错误响应。
	 * @param e 异常对象
	 * @return 包含错误信息的 ResponseEntity
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse> handleException(Exception e) {
		log.error("An error occurred: ", e);
		return ResponseEntity.internalServerError().body(ApiResponse.error("An error occurred: " + e.getMessage()));
	}

}
