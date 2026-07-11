package com.phoenix.data.service.hybrid.retrieval.impl;

import com.phoenix.data.dto.search.HybridSearchRequest;
import com.phoenix.data.service.hybrid.fusion.FusionStrategy;
import com.phoenix.data.service.hybrid.retrieval.AbstractHybridRetrievalStrategy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * PostgreSQL + pgvector 混合检索策略实现
 */
@Setter
@Slf4j
public class PgHyhridRetrievalStrategy extends AbstractHybridRetrievalStrategy{
    /** 相似度最小分数阈值 */
    private Double minScore;

    /**
     * 构造函数
     * @param executorService 异步执行器
     * @param vectorStore 向量存储
     * @param fusionStrategy 融合策略
     */
    public PgHyhridRetrievalStrategy(ExecutorService executorService, VectorStore vectorStore, FusionStrategy fusionStrategy) {
        super(executorService, vectorStore, fusionStrategy);
    }


    /**
     * 使用pgvector进行关键词搜索
     * @param request 混合搜索请求
     * @return 关键词匹配的文档列表
     */
    @Override
    public List<Document> getDocumentsByKeywords(HybridSearchRequest request) {
        SearchRequest.Builder builder = SearchRequest.builder().query(request.getQuery()).topK(request.getTopK()).similarityThreshold(minScore);
        if (request.getFilterExpression() != null) {
            builder.filterExpression(request.getFilterExpression());
        }
        SearchRequest searchRequest = builder.build();
        return vectorStore.similaritySearch(searchRequest);
    }

}
