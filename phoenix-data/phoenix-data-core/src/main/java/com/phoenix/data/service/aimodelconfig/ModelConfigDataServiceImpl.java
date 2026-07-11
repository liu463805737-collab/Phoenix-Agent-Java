package com.phoenix.data.service.aimodelconfig;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.data.converter.ModelConfigConverter;
import com.phoenix.data.dto.ModelConfigDTO;
import com.phoenix.data.entity.ModelConfig;
import com.phoenix.data.enums.ModelType;
import com.phoenix.data.mapper.ModelConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.phoenix.data.converter.ModelConfigConverter.toDTO;
import static com.phoenix.data.converter.ModelConfigConverter.toEntity;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ModelConfigDataServiceImpl extends ServiceImpl<ModelConfigMapper, ModelConfig> implements ModelConfigDataService {

	@Override
	@Transactional(readOnly = true)
	public ModelConfig findById(Integer id) {
		return getById(id);
	}

	@Override
	public void switchActiveStatus(Integer id, ModelType type) {
		getMapper().deactivateOthers(type.getCode(), id);

		ModelConfig entity = getById(id);
		if (entity != null) {
			entity.setIsActive(true);
			entity.setUpdatedTime(LocalDateTime.now());
			getMapper().update(entity);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<ModelConfigDTO> listConfigs() {
		return QueryChain.of(getMapper())
			.eq(ModelConfig::getIsDeleted, 0)
			.orderBy(ModelConfig::getCreatedTime, false)
			.list()
			.stream()
			.map(ModelConfigConverter::toDTO)
			.collect(Collectors.toList());
	}

	@Override
	public void addConfig(ModelConfigDTO dto) {
		clean(dto);
		save(toEntity(dto));
	}

	private void clean(ModelConfigDTO dto) {
		dto.setModelName(dto.getModelName().trim());
		dto.setBaseUrl(dto.getBaseUrl().trim());
		dto.setApiKey(dto.getApiKey().trim());
		if (dto.getCompletionsPath() != null) {
			dto.setCompletionsPath(dto.getCompletionsPath().trim());
		}
		if (dto.getEmbeddingsPath() != null) {
			dto.setEmbeddingsPath(dto.getEmbeddingsPath().trim());
		}
	}

	@Override
	public ModelConfig updateConfigInDb(ModelConfigDTO dto) {
		clean(dto);
		ModelConfig entity = getById(dto.getId());
		if (entity == null) {
			throw new RuntimeException("配置不存在");
		}

		if (!entity.getModelType().getCode().equals(dto.getModelType()))
			throw new RuntimeException("模型类型不允许修改");

		mergeDtoToEntity(dto, entity);
		entity.setUpdatedTime(LocalDateTime.now());

		getMapper().update(entity);

		return entity;
	}

	private static void mergeDtoToEntity(ModelConfigDTO dto, ModelConfig oldEntity) {
		oldEntity.setProvider(dto.getProvider());
		oldEntity.setBaseUrl(dto.getBaseUrl());
		oldEntity.setModelName(dto.getModelName());
		oldEntity.setTemperature(dto.getTemperature());
		oldEntity.setMaxTokens(dto.getMaxTokens());
		oldEntity.setCompletionsPath(dto.getCompletionsPath());
		oldEntity.setEmbeddingsPath(dto.getEmbeddingsPath());
		oldEntity.setUpdatedTime(LocalDateTime.now());
		oldEntity.setProxyEnabled(dto.getProxyEnabled());
		oldEntity.setProxyHost(dto.getProxyHost());
		oldEntity.setProxyPort(dto.getProxyPort());
		oldEntity.setProxyUsername(dto.getProxyUsername());
		oldEntity.setProxyPassword(dto.getProxyPassword());

		if (dto.getApiKey() != null && !dto.getApiKey().contains("****")) {
			oldEntity.setApiKey(dto.getApiKey());
		}
	}

	@Override
	public void deleteConfig(Integer id) {
		ModelConfig entity = getById(id);
		if (entity == null) {
			throw new RuntimeException("配置不存在");
		}

		if (Boolean.TRUE.equals(entity.getIsActive())) {
			throw new RuntimeException("该配置当前正在使用中，无法删除！请先激活其他配置，再进行删除操作。");
		}

		entity.setIsDeleted(1);
		entity.setUpdatedTime(LocalDateTime.now());
		int updated = getMapper().update(entity);
		if (updated == 0) {
			throw new RuntimeException("删除失败");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ModelConfigDTO getActiveConfigByType(ModelType modelType) {
		ModelConfig entity = getMapper().selectActiveByType(modelType.getCode());
		if (entity == null) {
			log.warn("Activation model configuration of type [{}] not found, attempting to downgrade...", modelType);
			return null;
		}
		return toDTO(entity);
	}

}
