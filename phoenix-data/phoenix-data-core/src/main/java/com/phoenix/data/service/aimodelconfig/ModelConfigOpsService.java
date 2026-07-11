package com.phoenix.data.service.aimodelconfig;

import com.phoenix.data.dto.ModelConfigDTO;
import com.phoenix.data.entity.ModelConfig;
import com.phoenix.data.enums.ModelType;
import com.phoenix.data.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 模型配置运维服务，提供配置的热切换、激活及连接测试功能。
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@AllArgsConstructor
public class ModelConfigOpsService {

	private final ModelConfigDataService modelConfigDataService;

	private final DynamicModelFactory modelFactory;

	private final AiModelRegistry aiModelRegistry;

	private final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

	/**
	 * 更新配置并热刷新内存中的模型实例
	 */
	public void updateAndRefresh(ModelConfigDTO dto) {
		// 1. 更新数据库
		ModelConfig entity = modelConfigDataService.updateConfigInDb(dto);

		// 2. 检查是否是激活状态
		if (Boolean.TRUE.equals(entity.getIsActive())) {
			try {
				// 3. 刷新内存模型
				log.info("Detected update on active config [{}], refreshing memory...", entity.getModelType());
				refreshMemoryModel(entity.getModelType());
			}
			catch (Exception e) {
				// 抛出异常回滚数据库事务
				throw new RuntimeException("配置更新失败: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * 激活指定 ID 的模型配置（先刷新内存，再更新数据库状态）
	 */
	public void activateConfig(Integer id) {
		// 1. 查数据
		ModelConfig entity = modelConfigDataService.findById(id);
		if (entity == null) {
			throw new RuntimeException("配置不存在");
		}

		// 2. 刷新内存模型
		log.info("Activating config ID={}, Type={}...", id, entity.getModelType());
		refreshMemoryModel(entity.getModelType());

		// 3. 更新数据库状态 (调用数据层)
		modelConfigDataService.switchActiveStatus(id, entity.getModelType());

		log.info("Config ID={} activated successfully.", id);
	}

	/**
	 * 刷新内存中的模型实例（清空注册中心的缓存）
	 */
	private void refreshMemoryModel(ModelType type) {
		if (ModelType.CHAT.equals(type)) {
			aiModelRegistry.refreshChat();
		}
		else if (ModelType.EMBEDDING.equals(type)) {
			aiModelRegistry.refreshEmbedding();
		}
		else {
			throw new RuntimeException("未知的模型类型: " + type);
		}
	}

	/**
	 * 测试模型连接（创建临时模型，不影响正在运行的模型）
	 */
	public void testConnection(ModelConfigDTO config) {
		String modelType = config.getModelType();

		try {
			if (ModelType.CHAT.getCode().equalsIgnoreCase(modelType)) {
				testChatModel(config);
			}
			else if (ModelType.EMBEDDING.getCode().equalsIgnoreCase(modelType)) {
				testEmbeddingModel(config);
			}
			else {
				throw new IllegalArgumentException("未知的模型类型: " + modelType);
			}
		}
		catch (Exception e) {
			try {
				log.error("Failed to test model connection. Config: {}", objectMapper.writeValueAsString(config), e);
			}
			catch (JsonProcessingException e1) {
				log.error("Failed to convert config to JSON. Config: {}", config, e1);
			}
			// 重新抛出异常，让 Controller 捕获并展示给前端
			// 如果是 OpenAiHttpException，通常包含具体的 API 错误信息
			throw new RuntimeException(parseErrorMessage(e));
		}
	}

	/**
	 * 测试 Chat 模型连接
	 */
	private void testChatModel(ModelConfigDTO config) {
		log.info("Testing Chat Model connection, provider: {}, modelName: {}", config.getProvider(),
				config.getModelName());

		// 1. 创建临时模型
		ChatModel tempModel = modelFactory.createChatModel(config);

		// 2. 发起最轻量的请求
		String promptText = "Hello";

		// 3. 调用
		String response = tempModel.call(promptText);

		// 4. 校验结果
		if (!StringUtils.hasText(response)) {
			throw new RuntimeException("模型返回内容为空");
		}
		log.info("Chat Model test passed. Response: {}", response);
	}

	/**
	 * 测试 Embedding 模型连接
	 */
	private void testEmbeddingModel(ModelConfigDTO config) {
		log.info("Testing Embedding Model connection, provider: {} modelName: {}", config.getProvider(),
				config.getModelName());
		// 1. 创建临时模型
		EmbeddingModel tempModel = modelFactory.createEmbeddingModel(config);

		// 2. 发起请求
		float[] embedding = tempModel.embed("Test");

		// 3. 校验结果
		if (embedding == null || embedding.length == 0) {
			throw new RuntimeException("模型生成的向量为空");
		}
		log.info("Embedding Model test passed. Dimension: {}", embedding.length);
	}

	/**
	 * 提取更友好的错误信息（处理常见的 HTTP 状态码）
	 */
	private String parseErrorMessage(Exception e) {
		// 如果是 401，通常是 Key 错
		if (e.getMessage().contains("401")) {
			return "鉴权失败 (401)，请检查 API Key 是否正确。";
		}
		// 如果是 404，通常是 BaseUrl 或 Path 错
		if (e.getMessage().contains("404")) {
			return "接口未找到 (404)，请检查 Base URL 或者路径配置地址。";
		}
		// 如果是 429，额度没了
		if (e.getMessage().contains("429")) {
			return "请求过多或余额不足 (429)，请检查厂商额度。";
		}
		// 其他错误直接返回原样
		return e.getMessage();
	}

}
