package com.phoenix.data.service.aimodelconfig;

import com.mybatisflex.core.service.IService;
import com.phoenix.data.dto.ModelConfigDTO;
import com.phoenix.data.entity.ModelConfig;
import com.phoenix.data.enums.ModelType;

import java.util.List;

/**
 * 模型配置数据服务接口，提供模型配置的增删改查及状态切换功能。
 */
public interface ModelConfigDataService extends IService<ModelConfig> {

	/**
	 * 根据 ID 查找模型配置
	 */
	ModelConfig findById(Integer id);

	/**
	 * 切换指定配置的激活状态（同类型其他配置将被禁用）
	 */
	void switchActiveStatus(Integer id, ModelType type);

	/**
	 * 获取所有模型配置 DTO 列表
	 */
	List<ModelConfigDTO> listConfigs();

	/**
	 * 新增模型配置
	 */
	void addConfig(ModelConfigDTO dto);

	/**
	 * 更新数据库中的模型配置（不处理热切换）
	 */
	ModelConfig updateConfigInDb(ModelConfigDTO dto);

	/**
	 * 删除模型配置（逻辑删除）
	 */
	void deleteConfig(Integer id);

	/**
	 * 获取指定类型的活跃模型配置
	 */
	ModelConfigDTO getActiveConfigByType(ModelType modelType);

}
