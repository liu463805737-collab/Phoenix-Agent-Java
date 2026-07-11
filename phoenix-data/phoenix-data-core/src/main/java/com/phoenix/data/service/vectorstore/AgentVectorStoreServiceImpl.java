package com.phoenix.data.service.vectorstore;

import cn.hutool.core.collection.ListUtil;
import com.phoenix.data.constant.Constant;
import com.phoenix.data.constant.DocumentMetadataConstant;
import com.phoenix.data.dto.search.AgentSearchRequest;
import com.phoenix.data.dto.search.HybridSearchRequest;
import com.phoenix.data.properties.DataAgentProperties;
import com.phoenix.data.service.hybrid.retrieval.HybridRetrievalStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;

import static com.phoenix.data.service.vectorstore.DynamicFilterService.buildFilterExpressionString;

/**
 * 智能体向量存储服务实现类，提供基于向量检索和混合检索的文档管理能力。
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AgentVectorStoreServiceImpl implements AgentVectorStoreService {

    private static final String DEFAULT = "default";
    @Autowired
    @Qualifier("simpleVectorStore")
    private VectorStore vectorStore;
    @Autowired
    private Optional<HybridRetrievalStrategy> hybridRetrievalStrategy;
    @Autowired
    private DataAgentProperties dataAgentProperties;
    @Autowired
    private DynamicFilterService dynamicFilterService;

    /**
     * 执行文档搜索，支持向量检索和混合检索两种模式
     *
     * @param searchRequest 搜索请求参数
     * @return 文档列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<Document> search(AgentSearchRequest searchRequest) {
        Assert.hasText(searchRequest.getAgentId(), "AgentId cannot be empty");
        Assert.hasText(searchRequest.getDocVectorType(), "DocVectorType cannot be empty");

        Filter.Expression filter = dynamicFilterService.buildDynamicFilter(searchRequest.getAgentId(),
                searchRequest.getDocVectorType());
        // 根据agentId vectorType找不到要 召回 的业务知识或者智能体知识
        if (filter == null) {
            log.warn(
                    "Dynamic filter returned null (no valid ids), returning empty result directly.AgentId: {}, VectorType: {}",
                    searchRequest.getAgentId(), searchRequest.getDocVectorType());
            return Collections.emptyList();
        }

        HybridSearchRequest hybridRequest = HybridSearchRequest.builder()
                .query(searchRequest.getQuery())
                .topK(searchRequest.getTopK())
                .similarityThreshold(searchRequest.getSimilarityThreshold())
                .filterExpression(filter)
                .build();

        if (dataAgentProperties.getVectorStore().isEnableHybridSearch() && hybridRetrievalStrategy.isPresent()) {
            return hybridRetrievalStrategy.get().retrieve(hybridRequest);
        }
        log.debug("Hybrid search is not enabled. use vector-search only");
        List<Document> results = vectorStore.similaritySearch(hybridRequest.toVectorSearchRequest());
        log.debug("Search completed with vectorType: {}, found {} documents for SearchRequest: {}",
                searchRequest.getDocVectorType(), results.size(), searchRequest);
        return results;

    }

    /**
     * 根据智能体 ID 和向量类型删除文档
     *
     * @param agentId    智能体 ID
     * @param vectorType 向量类型
     * @return 是否删除成功
     * @throws Exception 删除失败时抛出异常
     */
    @Override
    public Boolean deleteDocumentsByVectorType(String agentId, String vectorType) throws Exception {
        Assert.notNull(agentId, "AgentId cannot be null.");
        Assert.notNull(vectorType, "VectorType cannot be null.");

        Map<String, Object> metadata = new HashMap<>(Map.ofEntries(Map.entry(Constant.AGENT_ID, agentId),
                Map.entry(DocumentMetadataConstant.VECTOR_TYPE, vectorType)));

        return this.deleteDocumentsByMetedata(agentId, metadata);
    }

    /**
     * 为指定智能体添加文档，自动验证元数据一致性
     *
     * @param agentId   智能体 ID
     * @param documents 文档列表
     */
    @Override
    public void addDocuments(String agentId, List<Document> documents) {
        Assert.notNull(agentId, "AgentId cannot be null.");
        Assert.notEmpty(documents, "Documents cannot be empty.");

        // 验证文档中 metadata 的一致性
        for (Document document : documents) {
            Assert.notNull(document.getMetadata(), "Document metadata cannot be null.");

            String vectorType = (String) document.getMetadata().get(DocumentMetadataConstant.VECTOR_TYPE);

            // 根据 vectorType 验证不同的字段
            if (DocumentMetadataConstant.TABLE.equals(vectorType)
                    || DocumentMetadataConstant.COLUMN.equals(vectorType)) {
                // 表和列必须包含 datasourceId
                Assert.isTrue(document.getMetadata().containsKey(Constant.DATASOURCE_ID),
                        "Document metadata must contain datasourceId for TABLE/COLUMN type.");
            } else {
                // 知识库和业务术语必须包含 agentId
                Assert.isTrue(document.getMetadata().containsKey(Constant.AGENT_ID),
                        "Document metadata must contain agentId.");
                Assert.isTrue(document.getMetadata().get(Constant.AGENT_ID).equals(agentId),
                        "Document metadata agentId does not match.");
            }
        }
        List<List<Document>> spitDocments = ListUtil.partition(documents, 10);
        for (List<Document> spit : spitDocments) {
            vectorStore.add(spit);
        }
    }

    /**
     * 根据元数据条件删除文档
     *
     * @param metadata 元数据条件
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteDocumentsByMetadata(Map<String, Object> metadata) {
        Assert.notNull(metadata, "Metadata cannot be null.");
        String filterExpression = buildFilterExpressionString(metadata);

        if (vectorStore instanceof SimpleVectorStore) {
            batchDelDocumentsWithFilter(filterExpression);
        } else {
            vectorStore.delete(filterExpression);
        }

        return true;
    }

    /**
     * 根据智能体 ID 和元数据删除文档，自动补充 agentId 过滤条件
     *
     * @param agentId  智能体 ID
     * @param metadata 元数据条件
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteDocumentsByMetedata(String agentId, Map<String, Object> metadata) {
        Assert.hasText(agentId, "AgentId cannot be empty.");
        Assert.notNull(metadata, "Metadata cannot be null.");
        // 添加agentId元数据过滤条件, 用于删除指定agentId下的所有数据，因为metadata中用户调用可能忘记添加agentId
        metadata.put(Constant.AGENT_ID, agentId);
        String filterExpression = buildFilterExpressionString(metadata);
        vectorStore.delete(filterExpression);
        return true;
    }

    /**
     * 分批删除满足过滤条件的文档（SimpleVectorStore 专用）
     *
     * @param filterExpression 过滤表达式
     */
    private void batchDelDocumentsWithFilter(String filterExpression) {
        Set<String> seenDocumentIds = new HashSet<>();
        // 分批获取，因为Milvus等向量数据库的topK有限制
        List<Document> batch;
        int newDocumentsCount;
        int totalDeleted = 0;

        do {
            batch = vectorStore.similaritySearch(SearchRequest.builder()
                    .query(DEFAULT)// 使用默认的查询字符串，因为有的嵌入模型不支持空字符串
                    .filterExpression(filterExpression)
                    .similarityThreshold(0.0)// 设置最低相似度阈值以获取元数据匹配的所有文档
                    .topK(dataAgentProperties.getVectorStore().getBatchDelTopkLimit())
                    .build());

            // 过滤掉已经处理过的文档，只删除未处理的文档
            List<String> idsToDelete = new ArrayList<>();
            newDocumentsCount = 0;

            for (Document doc : batch) {
                if (seenDocumentIds.add(doc.getId())) {
                    // 如果add返回true，表示这是一个新的文档ID
                    idsToDelete.add(doc.getId());
                    newDocumentsCount++;
                }
            }

            // 删除这批新文档
            if (!idsToDelete.isEmpty()) {
                vectorStore.delete(idsToDelete);
                totalDeleted += idsToDelete.size();
            }

        }
        while (newDocumentsCount > 0); // 只有当获取到新文档时才继续循环

        log.info("Deleted {} documents with filter expression: {}", totalDeleted, filterExpression);
    }

    /**
     * 为指定智能体检索文档（使用默认 topK 和相似度阈值）
     *
     * @param agentId    智能体 ID
     * @param query      查询条件
     * @param vectorType 向量类型
     * @return 文档列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<Document> getDocumentsForAgent(String agentId, String query, String vectorType) {
        // 使用全局默认配置
        int defaultTopK = dataAgentProperties.getVectorStore().getDefaultTopkLimit();
        double defaultThreshold = dataAgentProperties.getVectorStore().getDefaultSimilarityThreshold();

        return getDocumentsForAgent(agentId, query, vectorType, defaultTopK, defaultThreshold);
    }

    /**
     * 为指定智能体检索文档（指定 topK 和相似度阈值）
     *
     * @param agentId    智能体 ID
     * @param query      查询条件
     * @param vectorType 向量类型
     * @param topK       返回数量上限
     * @param threshold  相似度阈值
     * @return 文档列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<Document> getDocumentsForAgent(String agentId, String query, String vectorType, int topK,
                                               double threshold) {
        AgentSearchRequest searchRequest = AgentSearchRequest.builder()
                .agentId(agentId)
                .docVectorType(vectorType)
                .query(query)
                .topK(topK) // 使用传入的参数
                .similarityThreshold(threshold) // 使用传入的参数
                .build();
        return search(searchRequest);
    }

    /**
     * 通过元数据过滤表达式精确查找文档
     *
     * @param filterExpression 过滤表达式
     * @param topK             返回数量上限
     * @return 文档列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<Document> getDocumentsOnlyByFilter(Filter.Expression filterExpression, Integer topK) {
        Assert.notNull(filterExpression, "filterExpression cannot be null.");
        if (topK == null)
            topK = dataAgentProperties.getVectorStore().getDefaultTopkLimit();
        SearchRequest searchRequest = SearchRequest.builder()
                .query(DEFAULT)
                .topK(topK)
                .filterExpression(filterExpression)
                .similarityThreshold(0.0)
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }

    /**
     * 检查指定智能体是否存在文档
     *
     * @param agentId 智能体 ID
     * @return 是否存在文档
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasDocuments(String agentId) {
        // 类似 MySQL 的 LIMIT 1,只检查是否存在文档
        List<Document> docs = vectorStore.similaritySearch(SearchRequest.builder()
                .query(DEFAULT)// 使用默认的查询字符串，因为有的嵌入模型不支持空字符串
                .filterExpression(buildFilterExpressionString(Map.of(Constant.AGENT_ID, agentId)))
                .topK(1) // 只获取1个文档
                .similarityThreshold(0.0)
                .build());
        return !docs.isEmpty();
    }

}
