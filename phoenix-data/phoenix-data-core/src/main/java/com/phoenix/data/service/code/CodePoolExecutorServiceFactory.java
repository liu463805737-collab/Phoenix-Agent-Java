package com.phoenix.data.service.code;

import com.phoenix.data.properties.CodeExecutorProperties;
import com.phoenix.data.service.code.impls.AiSimulationCodeExecutorService;
import com.phoenix.data.service.code.impls.DockerCodePoolExecutorService;
import com.phoenix.data.service.code.impls.LocalCodePoolExecutorService;
import com.phoenix.data.service.llm.LlmService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * 运行Python任务的容器池（工厂Bean）
 *
 * @author vlsmb
 * @since 2025/7/28
 */
@Component
@AllArgsConstructor
public class CodePoolExecutorServiceFactory implements FactoryBean<CodePoolExecutorService> {

	private final CodeExecutorProperties properties;

	private final LlmService llmService;

	/**
	 * 根据配置创建对应的代码执行器实例
	 */
	@Override
	public CodePoolExecutorService getObject() {
		return switch (properties.getCodePoolExecutor()) {
			case DOCKER -> new DockerCodePoolExecutorService(properties);
			case LOCAL -> new LocalCodePoolExecutorService(properties);
			case AI_SIMULATION -> new AiSimulationCodeExecutorService(llmService);
			default ->
				throw new IllegalStateException("This option does not have a corresponding implementation class yet.");
		};
	}

	/**
	 * 返回工厂 Bean 的类型
	 */
	@Override
	public Class<?> getObjectType() {
		return CodePoolExecutorService.class;
	}

}
