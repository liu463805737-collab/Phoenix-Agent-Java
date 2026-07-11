package com.phoenix.data.service.hybrid.retrieval.impl;

import com.phoenix.data.dto.search.HybridSearchRequest;
import com.phoenix.data.service.hybrid.fusion.FusionStrategy;
import com.phoenix.data.service.hybrid.retrieval.AbstractHybridRetrievalStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 适合测试以及没有继承实现AbstractHybridRetrievalStrategy的向量库（如Pg,milvus）使用，无关键词搜索能力
 */
@Slf4j
public class DefaultHybridRetrievalStrategy extends AbstractHybridRetrievalStrategy {

	/**
	 * 构造函数
	 * @param executorService 异步执行器
	 * @param vectorStore 向量存储
	 * @param fusionStrategy 融合策略
	 */
	public DefaultHybridRetrievalStrategy(ExecutorService executorService, VectorStore vectorStore,
			FusionStrategy fusionStrategy) {
		super(executorService, vectorStore, fusionStrategy);
	}

	/**
	 * 默认实现不执行关键词搜索，返回空列表
	 * @param agentSearchRequest 混合搜索请求
	 * @return 空列表
	 */
	@Override
	public List<Document> getDocumentsByKeywords(HybridSearchRequest agentSearchRequest) {
		// keyword默认不操作
		return Collections.emptyList();
	}

}
