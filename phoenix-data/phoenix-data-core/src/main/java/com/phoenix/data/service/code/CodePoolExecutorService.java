package com.phoenix.data.service.code;

/**
 * 运行Python任务的容器池接口
 *
 * @author vlsmb
 * @since 2025/7/12
 */
public interface CodePoolExecutorService {

	/**
	 * 运行任务
	 */
	TaskResponse runTask(TaskRequest request);

	/**
	 * 任务请求记录，包含代码、标准输入和依赖要求
	 */
	record TaskRequest(String code, String input, String requirement) {

	}

	/**
	 * 任务响应记录，包含执行状态、标准输出和错误信息
	 */
	record TaskResponse(boolean isSuccess, boolean executionSuccessButResultFailed, String stdOut, String stdErr,
			String exceptionMsg) {

		/**
		 * 创建异常响应
		 */
		public static TaskResponse exception(String msg) {
			return new TaskResponse(false, false, null, null, "An exception occurred while executing the task: " + msg);
		}

		/**
		 * 创建成功响应
		 */
		public static TaskResponse success(String stdOut) {
			return new TaskResponse(true, false, stdOut, null, null);
		}

		/**
		 * 创建代码执行失败响应（代码本身错误）
		 */
		public static TaskResponse failure(String stdOut, String stdErr) {
			return new TaskResponse(false, true, stdOut, stdErr, "StdErr: " + stdErr);
		}
	}

	/**
	 * 容器状态枚举
	 */
	enum State {

		READY, RUNNING, REMOVING

	}

}
