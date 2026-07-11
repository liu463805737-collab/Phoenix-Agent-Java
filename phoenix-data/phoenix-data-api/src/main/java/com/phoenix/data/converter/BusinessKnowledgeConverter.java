package com.phoenix.data.converter;

import com.phoenix.data.dto.knowledge.businessknowledge.CreateBusinessKnowledgeDTO;
import com.phoenix.data.entity.BusinessKnowledge;
import com.phoenix.data.enums.EmbeddingStatus;
import com.phoenix.data.vo.BusinessKnowledgeVO;
import org.springframework.stereotype.Component;

/**
 * 业务知识对象转换器
 */
@Component
public class BusinessKnowledgeConverter {

	/**
	 * 将实体转换为VO
	 *
	 * @param po 业务知识实体
	 * @return 业务知识VO
	 */
	public BusinessKnowledgeVO toVo(BusinessKnowledge po) {
		return BusinessKnowledgeVO.builder()
			.id(po.getId())
			.businessTerm(po.getBusinessTerm())
			.description(po.getDescription())
			.synonyms(po.getSynonyms())
			.isRecall(po.getIsRecall() == 1)
			.agentId(po.getAgentId())
			.createdTime(po.getCreatedTime())
			.updatedTime(po.getUpdatedTime())
			.embeddingStatus(po.getEmbeddingStatus() != null ? po.getEmbeddingStatus().getValue() : null)
			.errorMsg(po.getErrorMsg())
			.build();
	}

	/**
	 * 将创建DTO转换为实体
	 *
	 * @param dto 创建业务知识DTO
	 * @return 业务知识实体
	 */
	public BusinessKnowledge toEntityForCreate(CreateBusinessKnowledgeDTO dto) {
		return BusinessKnowledge.builder()
			.businessTerm(dto.getBusinessTerm())
			.description(dto.getDescription())
			.synonyms(dto.getSynonyms())
			.agentId(dto.getAgentId())
			.isRecall(dto.getIsRecall() ? 1 : 0)
			.isDeleted(0)
			.embeddingStatus(EmbeddingStatus.PROCESSING)
			.build();

	}

}
