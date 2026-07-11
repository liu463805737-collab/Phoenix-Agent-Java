package com.phoenix.data.properties;

import com.phoenix.data.enums.CodePoolExecutorEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.phoenix.data.constant.Constant.PROJECT_PROPERTIES_PREFIX;

/**
 * @author vlsmb
 * @since 2025/7/12
 */
@Getter
@Setter
@ConfigurationProperties(prefix = CodeExecutorProperties.CONFIG_PREFIX)
public class CodeExecutorProperties {

	public static final String CONFIG_PREFIX = PROJECT_PROPERTIES_PREFIX + ".code-executor";

	/**
	 * Specify implementation class of code container pool runtime service
	 */
	CodePoolExecutorEnum codePoolExecutor = CodePoolExecutorEnum.DOCKER;

	/**
	 * Service host, use default address if null
	 */
	String host = null;

	/**
	 * Image name, can customize image with common third-party dependencies to replace
	 * this configuration
	 */
	String imageName = "continuumio/anaconda3:latest";

	/**
	 * Container name prefix
	 */
	String containerNamePrefix = "nl2sql-python-exec-";

	/**
	 * Task blocking queue size
	 */
	Integer taskQueueSize = 5;

	/**
	 * Maximum number of core containers
	 */
	Integer coreContainerNum = 2;

	/**
	 * Maximum number of temporary containers
	 */
	Integer tempContainerNum = 2;

	/**
	 * Core thread count of thread pool
	 */
	Integer coreThreadSize = 5;

	/**
	 * Maximum thread count of thread pool
	 */
	Integer maxThreadSize = 5;

	/**
	 * Survival time of temporary containers, in minutes
	 */
	Integer tempContainerAliveTime = 5;

	/**
	 * Task survival time of thread pool, in seconds
	 */
	Long keepThreadAliveTime = 60L;

	/**
	 * Task blocking queue size of thread pool
	 */
	Integer threadQueueSize = 10;

	/**
	 * Maximum container memory, in MB
	 */
	Long limitMemory = 500L;

	/**
	 * Number of container CPU cores
	 */
	Long cpuCore = 1L;

	/**
	 * Python code execution time limit
	 */
	String codeTimeout = "60s";

	/**
	 * Maximum container runtime
	 */
	Long containerTimeout = 3000L;

	/**
	 * Container network mode
	 */
	String networkMode = "none";

	/**
	 * Python执行的最大重试次数
	 */
	Integer pythonMaxTriesCount = 5;

}
