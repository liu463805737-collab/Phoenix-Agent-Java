package com.phoenix.data.service.prompt;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.data.dto.prompt.PromptConfigDTO;
import com.phoenix.data.entity.UserPromptConfig;
import com.phoenix.data.mapper.UserPromptConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 用户提示词配置管理服务，提供提示词配置的 CRUD 操作，支持运行时配置更新。
 *
 * @author Makoto
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class UserPromptServiceImpl extends ServiceImpl<UserPromptConfigMapper, UserPromptConfig> implements UserPromptService {

	/**
	 * 创建或更新提示词配置，若启用则自动激活
	 * @param configDTO 配置数据传输对象
	 * @return 保存后的配置对象
	 */
	@Override
	public UserPromptConfig saveOrUpdateConfig(PromptConfigDTO configDTO) {
		log.info("保存或更新提示词优化配置：{}", configDTO);

		UserPromptConfig config;
		if (configDTO.id() != null) {
			// Update existing configuration
			config = getById(configDTO.id());
			if (config != null) {
				config.setName(configDTO.name());
				config.setAgentId(configDTO.agentId());
				config.setSystemPrompt(configDTO.optimizationPrompt());
				config.setEnabled(configDTO.enabled());
				config.setDescription(configDTO.description());
				config.setPriority(configDTO.priority() != null ? configDTO.priority() : 0);
				config.setDisplayOrder(configDTO.displayOrder() != null ? configDTO.displayOrder() : 0);
				getMapper().update(config);
			}
			else {
				// ID不存在，创建新配置
				config = new UserPromptConfig();
				config.setId(configDTO.id());
				config.setName(configDTO.name());
				config.setPromptType(configDTO.promptType());
				config.setAgentId(configDTO.agentId());
				config.setSystemPrompt(configDTO.optimizationPrompt());
				config.setEnabled(configDTO.enabled());
				config.setDescription(configDTO.description());
				config.setCreator(configDTO.creator());
				config.setPriority(configDTO.priority() != null ? configDTO.priority() : 0);
				config.setDisplayOrder(configDTO.displayOrder() != null ? configDTO.displayOrder() : 0);
				save(config);
			}
		}
		else {
			// Create new configuration
			config = new UserPromptConfig();
			config.setId(UUID.randomUUID().toString());
			config.setName(configDTO.name());
			config.setPromptType(configDTO.promptType());
			config.setAgentId(configDTO.agentId());
			config.setSystemPrompt(configDTO.optimizationPrompt());
			config.setEnabled(configDTO.enabled());
			config.setDescription(configDTO.description());
			config.setCreator(configDTO.creator());
			config.setPriority(configDTO.priority() != null ? configDTO.priority() : 0);
			config.setDisplayOrder(configDTO.displayOrder() != null ? configDTO.displayOrder() : 0);
			save(config);
		}

		// 如果配置启用，直接启用该配置（支持多个配置同时启用）
		if (Boolean.TRUE.equals(config.getEnabled())) {
			getMapper().enableById(config.getId());
			log.info("已启用提示词类型 [{}] 的配置：{}", config.getPromptType(), config.getId());
		}

		return config;
	}

	/**
	 * 根据 ID 获取配置
	 * @param id 配置 ID
	 * @return 配置对象
	 */
	@Override
	@Transactional(readOnly = true)
	public UserPromptConfig getConfigById(String id) {
		return getById(id);
	}

	/**
	 * 根据提示词类型和智能体获取所有启用的配置
	 * @param promptType 提示词类型
	 * @param agentId 智能体 ID
	 * @return 配置列表
	 */
	@Override
	@Transactional(readOnly = true)
	public List<UserPromptConfig> getActiveConfigsByType(String promptType, Long agentId) {
		return getMapper().getActiveConfigsByType(promptType, agentId);
	}

	/**
	 * 根据提示词类型和智能体获取启用的配置
	 * @param promptType 提示词类型
	 * @param agentId 智能体 ID
	 * @return 配置对象
	 */
	@Override
	@Transactional(readOnly = true)
	public UserPromptConfig getActiveConfigByType(String promptType, Long agentId) {
		return getMapper().selectActiveByPromptType(promptType, agentId);
	}

	/**
	 * 获取所有配置
	 * @return 配置列表
	 */
	@Override
	@Transactional(readOnly = true)
	public List<UserPromptConfig> getAllConfigs() {
		return list();
	}

	/**
	 * 根据提示词类型和智能体获取所有配置
	 * @param promptType 提示词类型
	 * @param agentId 智能体 ID
	 * @return 配置列表
	 */
	@Override
	@Transactional(readOnly = true)
	public List<UserPromptConfig> getConfigsByType(String promptType, Long agentId) {
		return getMapper().getConfigsByType(promptType, agentId);
	}

	/**
	 * 删除配置
	 * @param id 配置 ID
	 * @return 是否删除成功
	 */
	@Override
	public boolean deleteConfig(String id) {
		UserPromptConfig config = getById(id);
		if (config != null) {
			// 从数据库删除
			boolean deleted = removeById(id);
			if (deleted) {
				log.info("已删除配置：{}", id);
				return true;
			}
		}
		return false;
	}

	/**
	 * 启用指定配置
	 * @param id 配置 ID
	 * @return 操作是否成功
	 */
	@Override
	public boolean enableConfig(String id) {
		UserPromptConfig config = getById(id);
		if (config != null) {
			// Enable the current configuration (支持多个配置同时启用)
			int updated = getMapper().enableById(id);
			if (updated > 0) {
				log.info("已启用配置：{}", id);
				return true;
			}
		}
		return false;
	}

	/**
	 * 禁用指定配置
	 * @param id 配置 ID
	 * @return 操作是否成功
	 */
	@Override
	public boolean disableConfig(String id) {
		int updated = getMapper().disableById(id);
		if (updated > 0) {
			log.info("已禁用配置：{}", id);
			return true;
		}
		return false;
	}

	/**
	 * 根据提示词类型和智能体获取优化配置
	 * @param promptType 提示词类型
	 * @param agentId 智能体 ID
	 * @return 优化配置列表
	 */
	@Override
	@Transactional(readOnly = true)
	public List<UserPromptConfig> getOptimizationConfigs(String promptType, Long agentId) {
		return getActiveConfigsByType(promptType, agentId);
	}

	/**
	 * 批量启用配置
	 * @param ids 配置 ID 列表
	 * @return 操作结果
	 */
	@Override
	public boolean enableConfigs(List<String> ids) {
		for (String id : ids) {
			getMapper().enableById(id);
		}
		log.info("批量启用配置成功：{}", ids);
		return true;
	}

	/**
	 * 批量禁用配置
	 * @param ids 配置 ID 列表
	 * @return 操作结果
	 */
	@Override
	public boolean disableConfigs(List<String> ids) {
		for (String id : ids) {
			getMapper().disableById(id);
		}
		log.info("批量禁用配置成功：{}", ids);
		return true;
	}

	/**
	 * 更新配置优先级
	 * @param id 配置 ID
	 * @param priority 优先级
	 * @return 操作结果
	 */
	@Override
	public boolean updatePriority(String id, Integer priority) {
		UserPromptConfig config = getById(id);
		if (config != null) {
			config.setPriority(priority);
			getMapper().update(config);
			log.info("更新配置优先级成功：{} -> {}", id, priority);
			return true;
		}
		return false;
	}

	/**
	 * 更新配置显示顺序
	 * @param id 配置 ID
	 * @param displayOrder 显示顺序
	 * @return 操作结果
	 */
	@Override
	public boolean updateDisplayOrder(String id, Integer displayOrder) {
		UserPromptConfig config = getById(id);
		if (config != null) {
			config.setDisplayOrder(displayOrder);
			getMapper().update(config);
			log.info("更新配置显示顺序成功：{} -> {}", id, displayOrder);
			return true;
		}
		return false;
	}

}
