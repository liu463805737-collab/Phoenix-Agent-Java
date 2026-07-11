package com.phoenix.data.service.llm;

import com.phoenix.data.properties.DataAgentProperties;
import com.phoenix.data.service.aimodelconfig.AiModelRegistry;
import com.phoenix.data.service.llm.impls.BlockLlmService;
import com.phoenix.data.service.llm.impls.StreamLlmService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * LLM 服务工厂，根据配置创建对应的阻塞或流式 LLM 服务实例。
 */
@Component
@AllArgsConstructor
public class LlmServiceFactory implements FactoryBean<LlmService> {

	private final DataAgentProperties properties;

	private final AiModelRegistry aiModelRegistry;

	/**
	 * 根据配置创建 LlmService 实例
	 * @return LlmService 实例
	 */
	@Override
	public LlmService getObject() {
		if (com.phoenix.data.enums.LlmServiceEnum.BLOCK.equals(properties.getLlmServiceType())) {
			return new BlockLlmService(aiModelRegistry);
		}
		else {
			return new StreamLlmService(aiModelRegistry);
		}
	}

	/**
	 * 返回工厂创建的 Bean 类型
	 * @return LlmService 类型
	 */
	@Override
	public Class<?> getObjectType() {
		return LlmService.class;
	}

}
