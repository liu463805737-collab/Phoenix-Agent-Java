package com.phoenix.data.service.hybrid.retrieval;

import com.phoenix.data.dto.search.HybridSearchRequest;
import com.phoenix.data.service.hybrid.fusion.FusionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * 混合检索策略抽象基类，实现模板方法模式，提供向量搜索与关键词搜索的并行执行与结果融合
 */
@Slf4j
public abstract class AbstractHybridRetrievalStrategy implements HybridRetrievalStrategy {

	/** 异步执行器 */
	protected final ExecutorService executorService;

	/** 向量存储 */
	protected final VectorStore vectorStore;

	/** 融合策略 */
	protected final FusionStrategy fusionStrategy;

	/**
	 * 构造函数
	 * @param executorService 异步执行器
	 * @param vectorStore 向量存储
	 * @param fusionStrategy 融合策略
	 */
	protected AbstractHybridRetrievalStrategy(ExecutorService executorService, VectorStore vectorStore,
			FusionStrategy fusionStrategy) {
		this.executorService = executorService;
		this.vectorStore = vectorStore;
		this.fusionStrategy = fusionStrategy;
		log.info(
				"Initialized AbstractHybridRetrievalStrategy with executorService: {}, vectorStore: {}, fusionStrategy: {}",
				executorService, vectorStore, fusionStrategy);
	}

	// 模板方法，先进行向量搜索后进行关键词搜索，最后结果融合。
	// 如果你的向量库天然支持混合检索，如Milvus,Es..你可以在子类直接重写该方法不用它这里的流程走
	// 目前ES实现仍然按照模板流程走，因为ES的付费企业版才能使用它服务端的rrf融合策略
	/**
	 * 模板方法：并行执行向量搜索和关键词搜索，然后融合结果
	 * @param request 混合搜索请求
	 * @return 搜索后的文档列表
	 */
	@Override
	public List<Document> retrieve(HybridSearchRequest request) {

		SearchRequest vectorSearchRequest = request.toVectorSearchRequest();

		// 异步执行向量搜索
		CompletableFuture<List<Document>> vectorSearchFuture = CompletableFuture.supplyAsync(() -> {
			List<Document> vectorResults = vectorStore.similaritySearch(vectorSearchRequest);
			log.debug("Vector Search completed. Found {} documents for SearchRequest: {}", vectorResults.size(),
					vectorSearchRequest);
			return vectorResults;
		}, executorService);

		// 异步执行关键词搜索
		CompletableFuture<List<Document>> keywordSearchFuture = CompletableFuture.supplyAsync(() -> {
			List<Document> results = getDocumentsByKeywords(request);
			log.debug("Keyword Search completed. Found {} documents, with query: {}", results.size(),
					request.getQuery());
			return results;
		}, executorService);

		try {
			List<Document> vectorResults = vectorSearchFuture.get();

			// 等待关键词搜索完成
			List<Document> keywordResults = keywordSearchFuture.get();

			// 融合结果
			List<Document> finalDocuments = fusionStrategy.fuseResults(request.getTopK(), vectorResults,
					keywordResults);
			log.debug("Fusion completed. Found {} documents", finalDocuments.size());
			return finalDocuments;
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Search operation interrupted", e);
		}
		catch (ExecutionException e) {
			throw new RuntimeException("Error during parallel search execution", e);
		}

	}

	/**
	 * 根据关键词检索文档，由子类实现
	 * @param request 混合搜索请求
	 * @return 文档列表
	 */
	public abstract List<Document> getDocumentsByKeywords(HybridSearchRequest request);

}
