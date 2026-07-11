package com.phoenix.data.service.semantic;

import com.mybatisflex.core.service.IService;
import com.phoenix.data.dto.schema.SemanticModelAddDTO;
import com.phoenix.data.dto.schema.SemanticModelBatchImportDTO;
import com.phoenix.data.entity.SemanticModel;
import com.phoenix.data.vo.BatchImportResult;

import java.io.InputStream;
import java.util.List;

/**
 * 语义模型服务接口，定义语义模型的增删改查及导入导出等核心方法。
 */
public interface SemanticModelService extends IService<SemanticModel> {

	/**
	 * 获取所有语义模型
	 * @return 语义模型列表
	 */
	List<SemanticModel> getAll();

	/**
	 * 根据智能体 ID 获取所有启用的语义模型
	 * @param agentId 智能体 ID
	 * @return 语义模型列表
	 */
	List<SemanticModel> getEnabledByAgentId(Long agentId);

	/**
	 * 根据智能体 ID 和表名列表获取语义模型
	 * @param agentId 智能体 ID
	 * @param tableNames 表名列表
	 * @return 语义模型列表
	 */
	List<SemanticModel> getByAgentIdAndTableNames(Long agentId, List<String> tableNames);

	/**
	 * 根据 ID 获取语义模型
	 * @param id 语义模型 ID
	 * @return 语义模型
	 */
	SemanticModel getById(Long id);

	/**
	 * 添加语义模型
	 * @param semanticModel 语义模型实体
	 */
	void addSemanticModel(SemanticModel semanticModel);

	/**
	 * 通过 DTO 添加语义模型
	 * @param dto 语义模型添加参数
	 * @return 是否添加成功
	 */
	boolean addSemanticModel(SemanticModelAddDTO dto);

	/**
	 * 启用语义模型
	 * @param id 语义模型 ID
	 */
	void enableSemanticModel(Long id);

	/**
	 * 禁用语义模型
	 * @param id 语义模型 ID
	 */
	void disableSemanticModel(Long id);

	/**
	 * 根据智能体 ID 获取所有语义模型
	 * @param agentId 智能体 ID
	 * @return 语义模型列表
	 */
	List<SemanticModel> getByAgentId(Long agentId);

	/**
	 * 根据关键字搜索语义模型
	 * @param keyword 搜索关键字
	 * @return 语义模型列表
	 */
	List<SemanticModel> search(String keyword);

	/**
	 * 删除语义模型
	 * @param id 语义模型 ID
	 */
	void deleteSemanticModel(Long id);

	/**
	 * 更新语义模型
	 * @param id 语义模型 ID
	 * @param semanticModel 新的语义模型数据
	 */
	void updateSemanticModel(Long id, SemanticModel semanticModel);

	/**
	 * 批量添加语义模型
	 * @param semanticModels 语义模型列表
	 */
	default void addSemanticModels(List<SemanticModel> semanticModels) {
		semanticModels.forEach(this::addSemanticModel);
	}

	/**
	 * 批量启用语义模型
	 * @param ids 语义模型 ID 列表
	 */
	default void enableSemanticModels(List<Long> ids) {
		ids.forEach(this::enableSemanticModel);
	}

	/**
	 * 批量禁用语义模型
	 * @param ids 语义模型 ID 列表
	 */
	default void disableSemanticModels(List<Long> ids) {
		ids.forEach(this::disableSemanticModel);
	}

	/**
	 * 批量删除语义模型
	 * @param ids 语义模型 ID 列表
	 */
	default void deleteSemanticModels(List<Long> ids) {
		ids.forEach(this::deleteSemanticModel);
	}

	/**
	 * 批量导入语义模型
	 * @param dto 批量导入参数
	 * @return 导入结果
	 */
	BatchImportResult batchImport(SemanticModelBatchImportDTO dto);

	/**
	 * 从Excel文件导入语义模型
	 * @param inputStream Excel文件输入流
	 * @param filename 文件名
	 * @param agentId 智能体ID
	 * @return 导入结果
	 */
	BatchImportResult importFromExcel(InputStream inputStream, String filename, Long agentId);

}
