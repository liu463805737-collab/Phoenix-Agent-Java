package com.phoenix.data.service.business;

import com.mybatisflex.core.service.IService;
import com.phoenix.data.dto.knowledge.businessknowledge.CreateBusinessKnowledgeDTO;
import com.phoenix.data.dto.knowledge.businessknowledge.UpdateBusinessKnowledgeDTO;
import com.phoenix.data.entity.BusinessKnowledge;
import com.phoenix.data.vo.BusinessKnowledgeVO;

import java.util.List;

/**
 * 业务知识服务接口，提供业务知识词的增删改查、向量存储同步及召回管理功能。
 */
public interface BusinessKnowledgeService extends IService<BusinessKnowledge> {

	/**
	 * 根据 Agent ID 获取业务知识列表
	 */
	List<BusinessKnowledgeVO> getKnowledge(Long agentId);

	/**
	 * 获取所有业务知识列表
	 */
	List<BusinessKnowledgeVO> getAllKnowledge();

	/**
	 * 根据关键字搜索指定 Agent 的业务知识
	 */
	List<BusinessKnowledgeVO> searchKnowledge(Long agentId, String keyword);

	/**
	 * 根据 ID 获取业务知识
	 */
	BusinessKnowledgeVO getKnowledgeById(Long id);

	/**
	 * 新增业务知识（同时写入向量存储）
	 */
	BusinessKnowledgeVO addKnowledge(CreateBusinessKnowledgeDTO knowledgeDTO);

	/**
	 * 更新业务知识（同时同步向量存储）
	 */
	BusinessKnowledgeVO updateKnowledge(Long id, UpdateBusinessKnowledgeDTO knowledgeDTO);

	/**
	 * 删除业务知识（逻辑删除，同时清理向量存储）
	 */
	void deleteKnowledge(Long id);

	/**
	 * 切换业务知识的召回状态
	 */
	void recallKnowledge(Long id, Boolean isRecall);

	/**
	 * 刷新指定 Agent 的所有召回知识到向量存储
	 */
	void refreshAllKnowledgeToVectorStore(String agentId) throws Exception;

	/**
	 * 重试失败的业务知识嵌入
	 */
	void retryEmbedding(Long id);

}
