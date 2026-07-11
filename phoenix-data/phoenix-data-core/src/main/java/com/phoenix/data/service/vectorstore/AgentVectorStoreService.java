package com.phoenix.data.service.vectorstore;

import com.phoenix.data.dto.search.AgentSearchRequest;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.filter.Filter;

import java.util.List;
import java.util.Map;

/**
 * 智能体向量存储服务接口，定义文档的搜索、删除、添加等核心操作。
 */
public interface AgentVectorStoreService {

	/**
	 * 查询某个Agent的文档 总入口
	 */
	List<Document> search(AgentSearchRequest searchRequest);

	/**
	 * 根据智能体 ID 和向量类型删除文档
	 * @param agentId 智能体 ID
	 * @param vectorType 向量类型
	 * @return 是否删除成功
	 * @throws Exception 删除失败时抛出异常
	 */
	Boolean deleteDocumentsByVectorType(String agentId, String vectorType) throws Exception;

	/**
	 * 根据智能体 ID 和元数据删除文档
	 * @param agentId 智能体 ID
	 * @param metadata 元数据条件
	 * @return 是否删除成功
	 */
	Boolean deleteDocumentsByMetedata(String agentId, Map<String, Object> metadata);

	/**
	 * 根据元数据条件删除文档
	 * @param metadata 元数据条件
	 * @return 是否删除成功
	 */
	Boolean deleteDocumentsByMetadata(Map<String, Object> metadata);

	/**
	 * Get documents for specified agent
	 */
	List<Document> getDocumentsForAgent(String agentId, String query, String vectorType);

	/**
	 * 为指定智能体检索文档（带 topK 和相似度阈值）
	 * @param agentId 智能体 ID
	 * @param query 查询条件
	 * @param vectorType 向量类型
	 * @param topK 返回数量上限
	 * @param threshold 相似度阈值
	 * @return 文档列表
	 */
	List<Document> getDocumentsForAgent(String agentId, String query, String vectorType, int topK, double threshold);

	/**
	 * 通过元数据过滤表达式精确查找文档
	 * @param filterExpression 过滤表达式
	 * @param topK 返回数量上限
	 * @return 文档列表
	 */
	List<Document> getDocumentsOnlyByFilter(Filter.Expression filterExpression, Integer topK);

	/**
	 * 检查指定智能体是否存在文档
	 * @param agentId 智能体 ID
	 * @return 是否存在文档
	 */
	boolean hasDocuments(String agentId);

	/**
	 * 为指定智能体添加文档
	 * @param agentId 智能体 ID
	 * @param documents 文档列表
	 */
	void addDocuments(String agentId, List<Document> documents);

}
