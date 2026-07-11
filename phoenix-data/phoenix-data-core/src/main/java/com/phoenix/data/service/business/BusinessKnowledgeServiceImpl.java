package com.phoenix.data.service.business;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.data.constant.Constant;
import com.phoenix.data.constant.DocumentMetadataConstant;
import com.phoenix.data.converter.BusinessKnowledgeConverter;
import com.phoenix.data.dto.knowledge.businessknowledge.CreateBusinessKnowledgeDTO;
import com.phoenix.data.dto.knowledge.businessknowledge.UpdateBusinessKnowledgeDTO;
import com.phoenix.data.entity.BusinessKnowledge;
import com.phoenix.data.enums.EmbeddingStatus;
import com.phoenix.data.mapper.BusinessKnowledgeMapper;
import com.phoenix.data.service.vectorstore.AgentVectorStoreService;
import com.phoenix.data.util.DocumentConverterUtil;
import com.phoenix.data.vo.BusinessKnowledgeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务知识服务实现类，处理业务知识的 CRUD、向量存储同步及嵌入重试逻辑。
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class BusinessKnowledgeServiceImpl extends ServiceImpl<BusinessKnowledgeMapper, BusinessKnowledge> implements BusinessKnowledgeService {

	private final AgentVectorStoreService agentVectorStoreService;

	private final BusinessKnowledgeConverter businessKnowledgeConverter;

	public BusinessKnowledgeServiceImpl(AgentVectorStoreService agentVectorStoreService,
			BusinessKnowledgeConverter businessKnowledgeConverter) {
		this.agentVectorStoreService = agentVectorStoreService;
		this.businessKnowledgeConverter = businessKnowledgeConverter;
	}

	/**
	 * 根据 Agent ID 获取业务知识列表
	 */
	@Override
	@Transactional(readOnly = true)
	public List<BusinessKnowledgeVO> getKnowledge(Long agentId) {
		List<BusinessKnowledge> businessKnowledges = QueryChain.of(getMapper())
			.eq(BusinessKnowledge::getAgentId, agentId)
			.eq(BusinessKnowledge::getIsDeleted, 0)
			.orderBy(BusinessKnowledge::getCreatedTime, false)
			.list();
		if (CollectionUtils.isEmpty(businessKnowledges)) {
			return Collections.emptyList();
		}
		return businessKnowledges.stream().map(businessKnowledgeConverter::toVo).toList();
	}

	/**
	 * 获取所有业务知识列表
	 */
	@Override
	@Transactional(readOnly = true)
	public List<BusinessKnowledgeVO> getAllKnowledge() {
		List<BusinessKnowledge> businessKnowledges = QueryChain.of(getMapper())
			.eq(BusinessKnowledge::getIsDeleted, 0)
			.orderBy(BusinessKnowledge::getCreatedTime, false)
			.list();
		if (CollectionUtils.isEmpty(businessKnowledges)) {
			return Collections.emptyList();
		}
		return businessKnowledges.stream().map(businessKnowledgeConverter::toVo).toList();
	}

	/**
	 * 根据关键字搜索指定 Agent 的业务知识
	 */
	@Override
	@Transactional(readOnly = true)
	public List<BusinessKnowledgeVO> searchKnowledge(Long agentId, String keyword) {
		List<BusinessKnowledge> businessKnowledges = getMapper().searchInAgent(agentId, keyword);
		if (CollectionUtils.isEmpty(businessKnowledges)) {
			return Collections.emptyList();
		}
		return businessKnowledges.stream().map(businessKnowledgeConverter::toVo).toList();
	}

	/**
	 * 根据 ID 获取业务知识
	 */
	@Override
	@Transactional(readOnly = true)
	public BusinessKnowledgeVO getKnowledgeById(Long id) {
		BusinessKnowledge businessKnowledge = getById(id);
		if (businessKnowledge == null) {
			return null;
		}
		return businessKnowledgeConverter.toVo(businessKnowledge);
	}

	/**
	 * 新增业务知识，写入数据库并同步到向量存储
	 */
	@Override
	public BusinessKnowledgeVO addKnowledge(CreateBusinessKnowledgeDTO knowledgeDTO) {
		BusinessKnowledge entity = businessKnowledgeConverter.toEntityForCreate(knowledgeDTO);

		// 插入数据库
		if (!save(entity)) {
			throw new RuntimeException("Failed to add knowledge to database");
		}

		try {
			Document document = DocumentConverterUtil.convertBusinessKnowledgeToDocument(entity);
			agentVectorStoreService.addDocuments(entity.getAgentId().toString(), List.of(document));
			entity.setEmbeddingStatus(EmbeddingStatus.COMPLETED);
			entity.setErrorMsg(null);
			getMapper().update(entity);
		}
		catch (Exception e) {
			String errorMsg = "Failed to add to vector store: " + e.getMessage();
			entity.setEmbeddingStatus(EmbeddingStatus.FAILED);
			entity.setErrorMsg(errorMsg);
			getMapper().update(entity);
			log.error("Failed to add knowledge to vector store for id: {}, error: {}", entity.getId(), errorMsg);
		}
		return businessKnowledgeConverter.toVo(entity);
	}

	/**
	 * 更新业务知识，同步更新向量存储
	 */
	@Override
	public BusinessKnowledgeVO updateKnowledge(Long id, UpdateBusinessKnowledgeDTO knowledgeDTO) {
		// 从数据库获取原始数据
		BusinessKnowledge knowledge = getById(id);
		if (knowledge == null) {
			throw new RuntimeException("Knowledge not found with id: " + id);
		}
		// 更新属性
		knowledge.setBusinessTerm(knowledgeDTO.getBusinessTerm());
		knowledge.setDescription(knowledgeDTO.getDescription());
		if (StringUtils.hasText(knowledgeDTO.getSynonyms()))
			knowledge.setSynonyms(knowledgeDTO.getSynonyms());

		// 设置初始状态为处理中
		knowledge.setEmbeddingStatus(EmbeddingStatus.PROCESSING);

		// 先更新数据库
		if (getMapper().update(knowledge) <= 0) {
			throw new RuntimeException("Failed to update knowledge in database");
		}

		// 尝试更新向量库
		try {
			syncToVectorStore(knowledge);
			knowledge.setEmbeddingStatus(EmbeddingStatus.COMPLETED);
			knowledge.setErrorMsg(null);
			getMapper().update(knowledge);
		}
		catch (Exception e) {
			// 向量库更新失败，不回滚MySQL，只标记状态为失败
			String errorMsg = "Failed to update vector store: " + e.getMessage();
			knowledge.setEmbeddingStatus(EmbeddingStatus.FAILED);
			knowledge.setErrorMsg(errorMsg);
			getMapper().update(knowledge);
			log.error("Failed to update vector store for knowledge id: {}, error: {}", id, errorMsg);
		}
		return businessKnowledgeConverter.toVo(knowledge);
	}

	/**
	 * 同步单条业务知识到向量存储（先删后加）
	 */
	private void syncToVectorStore(BusinessKnowledge knowledge) {
		// 先删除旧的向量数据
		this.doDelVector(knowledge);

		// 添加新的向量数据
		Document newDocument = DocumentConverterUtil.convertBusinessKnowledgeToDocument(knowledge);
		agentVectorStoreService.addDocuments(knowledge.getAgentId().toString(), List.of(newDocument));

		log.info("Successfully updated vector store for knowledge id: {}", knowledge.getId());
	}

	/**
	 * 逻辑删除业务知识并清理向量存储
	 */
	@Override
	public void deleteKnowledge(Long id) {
		// 从数据库获取原始数据
		BusinessKnowledge knowledge = getById(id);
		if (knowledge == null) {
			log.warn("Knowledge not found with id: " + id);
			return;
		}

		doDelVector(knowledge);

		if (getMapper().logicalDelete(id, 1) <= 0) {
			// 重新添加修复被删除的记录
			agentVectorStoreService.addDocuments(knowledge.getAgentId().toString(),
					List.of(DocumentConverterUtil.convertBusinessKnowledgeToDocument(knowledge)));
			throw new RuntimeException("Failed to logically delete knowledge from database");
		}
	}

	/**
	 * 根据元数据删除向量存储中的业务知识
	 */
	private void doDelVector(BusinessKnowledge knowledge) {
		Map<String, Object> metadata = new HashMap<>();
		metadata.put(Constant.AGENT_ID, knowledge.getAgentId().toString());
		metadata.put(DocumentMetadataConstant.DB_BUSINESS_TERM_ID, knowledge.getId());
		metadata.put(DocumentMetadataConstant.VECTOR_TYPE, DocumentMetadataConstant.BUSINESS_TERM);
		agentVectorStoreService.deleteDocumentsByMetedata(knowledge.getAgentId().toString(), metadata);
	}

	/**
	 * 切换业务知识的召回状态（仅更新数据库，不影响向量存储）
	 */
	@Override
	public void recallKnowledge(Long id, Boolean isRecall) {
		// 从数据库获取原始数据
		BusinessKnowledge knowledge = getById(id);
		if (knowledge == null) {
			throw new RuntimeException("Knowledge not found with id: " + id);
		}

		// 更新数据库即可，不需要更新向量库，混合检索的的时候DynamicFilterService会根据 isRecall 字段过滤了
		knowledge.setIsRecall(isRecall ? 1 : 0);
		getMapper().update(knowledge);

	}

	/**
	 * 刷新指定 Agent 的所有启用召回的知识到向量存储
	 */
	@Override
	public void refreshAllKnowledgeToVectorStore(String agentId) throws Exception {
		agentVectorStoreService.deleteDocumentsByVectorType(agentId, DocumentMetadataConstant.BUSINESS_TERM);

		// 获取所有 isRecall 等于 1 且未逻辑删除的 BusinessKnowledge
		List<BusinessKnowledge> allKnowledge = QueryChain.of(getMapper())
			.eq(BusinessKnowledge::getIsDeleted, 0)
			.orderBy(BusinessKnowledge::getCreatedTime, false)
			.list();
		List<BusinessKnowledge> recalledKnowledge = allKnowledge.stream()
			.filter(knowledge -> knowledge.getIsRecall() != null && knowledge.getIsRecall() == 1)
			.filter(knowledge -> knowledge.getIsDeleted() == null || knowledge.getIsDeleted() == 0)
			.filter(knowledge -> agentId.equals(knowledge.getAgentId().toString()))
			.toList();

		// 转换为 Document 并插入到 vectorStore
		if (!recalledKnowledge.isEmpty()) {
			List<Document> documents = recalledKnowledge.stream()
				.map(DocumentConverterUtil::convertBusinessKnowledgeToDocument)
				.toList();
			agentVectorStoreService.addDocuments(agentId, documents);
		}
	}

	/**
	 * 重试失败的嵌入操作
	 */
	@Override
	public void retryEmbedding(Long id) {
		BusinessKnowledge knowledge = getById(id);
		if (knowledge == null) {
			throw new RuntimeException("BusinessKnowledge not found with id: " + id);
		}

		if (knowledge.getEmbeddingStatus().equals(EmbeddingStatus.PROCESSING)) {
			throw new RuntimeException("BusinessKnowledge is processing, please wait.");
		}

		// 非召回的不处理
		if (knowledge.getIsRecall() == null || knowledge.getIsRecall() == 0) {
			throw new RuntimeException("BusinessKnowledge is not recalled, please recall it first.");
		}

		try {
			syncToVectorStore(knowledge);
			knowledge.setEmbeddingStatus(EmbeddingStatus.COMPLETED);
			knowledge.setErrorMsg(null);
			getMapper().update(knowledge);
		}
		catch (Exception e) {
			// 再次失败，更新错误信息
			knowledge.setEmbeddingStatus(EmbeddingStatus.FAILED);
			knowledge.setErrorMsg(e.getMessage().length() > 200 ? e.getMessage().substring(0, 200) : e.getMessage());
			getMapper().update(knowledge);
			throw new RuntimeException("重试失败: " + e.getMessage());
		}

	}

}
