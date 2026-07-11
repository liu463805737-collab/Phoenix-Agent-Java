package com.phoenix.data.service.knowledge;

import com.mybatisflex.core.service.IService;
import com.phoenix.data.dto.knowledge.agentknowledge.AgentKnowledgeQueryDTO;
import com.phoenix.data.dto.knowledge.agentknowledge.CreateKnowledgeDTO;
import com.phoenix.data.dto.knowledge.agentknowledge.UpdateKnowledgeDTO;
import com.phoenix.data.entity.AgentKnowledge;
import com.phoenix.data.vo.AgentKnowledgeVO;
import com.phoenix.data.vo.PageResult;

/**
 * 智能体知识服务接口
 */
public interface AgentKnowledgeService extends IService<AgentKnowledge> {

	/**
	 * 根据ID获取知识
	 * @param id 知识ID
	 * @return 知识视图对象
	 */
	AgentKnowledgeVO getKnowledgeById(Integer id);

	/**
	 * 创建知识
	 * @param createKnowledgeDto 创建知识DTO
	 * @return 知识视图对象
	 */
	AgentKnowledgeVO createKnowledge(CreateKnowledgeDTO createKnowledgeDto);

	/**
	 * 更新知识
	 * @param id 知识ID
	 * @param updateKnowledgeDto 更新知识DTO
	 * @return 知识视图对象
	 */
	AgentKnowledgeVO updateKnowledge(Integer id, UpdateKnowledgeDTO updateKnowledgeDto);

	/**
	 * 删除知识（软删除）
	 * @param id 知识ID
	 * @return 是否删除成功
	 */
	boolean deleteKnowledge(Integer id);

	/**
	 * 分页条件查询知识列表
	 * @param queryDTO 查询条件
	 * @return 分页结果
	 */
	PageResult<AgentKnowledgeVO> queryByConditionsWithPage(AgentKnowledgeQueryDTO queryDTO);

	/**
	 * 更新知识的召回状态
	 * @param id 知识ID
	 * @param recalled 是否召回
	 * @return 知识视图对象
	 */
	AgentKnowledgeVO updateKnowledgeRecallStatus(Integer id, Boolean recalled);

	/**
	 * 重试知识向量化
	 * @param id 知识ID
	 */
	void retryEmbedding(Integer id);

}
