package com.phoenix.data.converter;

import com.phoenix.data.dto.ModelConfigDTO;
import com.phoenix.data.entity.ModelConfig;
import com.phoenix.data.enums.ModelType;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

/**
 * 模型配置对象转换器
 */
public class ModelConfigConverter {

	/**
	 * Entity -> DTO 用于把数据库数据转给前端看
	 */
	public static ModelConfigDTO toDTO(ModelConfig entity) {
		if (entity == null) {
			return null;
		}
		return ModelConfigDTO.builder()
			.id(entity.getId())
			.provider(entity.getProvider())
			.baseUrl(entity.getBaseUrl())
			.modelName(entity.getModelName())
			.temperature(entity.getTemperature())
			.maxTokens(entity.getMaxTokens())
			.isActive(entity.getIsActive())
			.apiKey(entity.getApiKey())
			.modelType(entity.getModelType().getCode())
			.completionsPath(entity.getCompletionsPath())
			.embeddingsPath(entity.getEmbeddingsPath())
			.proxyEnabled(entity.getProxyEnabled())
			.proxyHost(entity.getProxyHost())
			.proxyPort(entity.getProxyPort())
			.proxyUsername(entity.getProxyUsername())
			.proxyPassword(entity.getProxyPassword())
			.build();
	}

	/**
	 * DTO -> Entity 用于新增配置
	 */
	public static ModelConfig toEntity(ModelConfigDTO dto) {
		Assert.notNull(dto, "ModelConfigDTO cannot be null.");
		ModelConfig entity = new ModelConfig();
		// 新增时 ID 由数据库生成，所以这里通常不设置 ID，或者仅当 dto.id 有值时设置
		entity.setId(dto.getId());
		entity.setProvider(dto.getProvider());
		entity.setBaseUrl(dto.getBaseUrl());
		// 新增时，DTO 里的 Key 肯定是明文，直接存
		entity.setApiKey(dto.getApiKey());
		entity.setModelName(dto.getModelName());
		entity.setTemperature(dto.getTemperature());
		entity.setMaxTokens(dto.getMaxTokens());
		entity.setModelType(ModelType.fromCode(dto.getModelType()));
		entity.setCompletionsPath(dto.getCompletionsPath());
		entity.setEmbeddingsPath(dto.getEmbeddingsPath());
		entity.setProxyEnabled(dto.getProxyEnabled());
		entity.setProxyHost(dto.getProxyHost());
		entity.setProxyPort(dto.getProxyPort());
		entity.setProxyUsername(dto.getProxyUsername());
		entity.setProxyPassword(dto.getProxyPassword());
		// 默认值处理
		entity.setIsActive(false);
		entity.setIsDeleted(0);
		entity.setCreatedTime(LocalDateTime.now());
		entity.setUpdatedTime(LocalDateTime.now());

		return entity;
	}

}
