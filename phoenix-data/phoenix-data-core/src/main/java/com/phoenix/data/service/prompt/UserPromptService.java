package com.phoenix.data.service.prompt;

import com.mybatisflex.core.service.IService;
import com.phoenix.data.dto.prompt.PromptConfigDTO;
import com.phoenix.data.entity.UserPromptConfig;

import java.util.List;

/**
 * 用户提示词配置服务接口，提供提示词配置的 CRUD 及启用/禁用管理。
 */
public interface UserPromptService extends IService<UserPromptConfig> {

	/**
	 * 创建或更新提示词配置
	 * @param configDTO 配置数据传输对象
	 * @return 保存后的配置对象
	 */
	UserPromptConfig saveOrUpdateConfig(PromptConfigDTO configDTO);

	/**
	 * 根据 ID 获取配置
	 * @param id 配置 ID
	 * @return 配置对象，不存在时返回 null
	 */
	UserPromptConfig getConfigById(String id);

	/**
	 * 根据提示词类型和智能体获取所有启用的配置
	 * @param promptType 提示词类型
	 * @param agentId 智能体 ID，为空表示不按智能体过滤
	 * @return 配置列表
	 */
	List<UserPromptConfig> getActiveConfigsByType(String promptType, Long agentId);

	/**
	 * 根据提示词类型和智能体获取启用的配置
	 * @param promptType 提示词类型
	 * @param agentId 智能体 ID，为空表示不按智能体过滤
	 * @return 配置对象，不存在时返回 null
	 */
	UserPromptConfig getActiveConfigByType(String promptType, Long agentId);

	/**
	 * 获取所有配置
	 * @return 配置列表
	 */
	List<UserPromptConfig> getAllConfigs();

	/**
	 * 根据提示词类型和智能体获取所有配置
	 * @param promptType 提示词类型
	 * @param agentId 智能体 ID，为空表示不按智能体过滤
	 * @return 配置列表
	 */
	List<UserPromptConfig> getConfigsByType(String promptType, Long agentId);

	/**
	 * 删除配置
	 * @param id 配置 ID
	 * @return 是否删除成功
	 */
	boolean deleteConfig(String id);

	/**
	 * 启用指定配置
	 * @param id 配置 ID
	 * @return 操作是否成功
	 */
	boolean enableConfig(String id);

	/**
	 * 禁用指定配置
	 * @param id 配置 ID
	 * @return 操作是否成功
	 */
	boolean disableConfig(String id);

	/**
	 * 根据提示词类型和智能体获取优化配置
	 * @param promptType 提示词类型
	 * @param agentId 智能体 ID，为空表示不按智能体过滤
	 * @return 优化配置列表
	 */
	List<UserPromptConfig> getOptimizationConfigs(String promptType, Long agentId);

	/**
	 * 批量启用配置
	 * @param ids 配置 ID 列表
	 * @return 操作结果
	 */
	boolean enableConfigs(List<String> ids);

	/**
	 * 批量禁用配置
	 * @param ids 配置 ID 列表
	 * @return 操作结果
	 */
	boolean disableConfigs(List<String> ids);

	/**
	 * 更新配置优先级
	 * @param id 配置 ID
	 * @param priority 优先级
	 * @return 操作结果
	 */
	boolean updatePriority(String id, Integer priority);

	/**
	 * 更新配置显示顺序
	 * @param id 配置 ID
	 * @param displayOrder 显示顺序
	 * @return 操作结果
	 */
	boolean updateDisplayOrder(String id, Integer displayOrder);

}
