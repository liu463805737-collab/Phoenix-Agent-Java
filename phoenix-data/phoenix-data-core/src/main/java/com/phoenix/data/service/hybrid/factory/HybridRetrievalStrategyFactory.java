package com.phoenix.data.service.hybrid.factory;

import com.phoenix.data.properties.DataAgentProperties;
import com.phoenix.data.service.hybrid.fusion.FusionStrategy;
import com.phoenix.data.service.hybrid.retrieval.HybridRetrievalStrategy;
import com.phoenix.data.service.hybrid.retrieval.impl.ElasticsearchHybridRetrievalStrategy;
import com.phoenix.data.service.hybrid.retrieval.impl.PgHyhridRetrievalStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/**
 * 混合检索策略工厂类 根据配置条件创建并注册相应的 HybridRetrievalStrategy 实现类
 */
@Slf4j
@Component
public class HybridRetrievalStrategyFactory implements FactoryBean<HybridRetrievalStrategy> {

	// spring ai 的官方属性
	@Value("${spring.ai.vectorstore.type:simple}")
	/** 向量存储类型 */
	private String vectorStoreType;

	/** Elasticsearch索引名称 */
	@Value("${spring.ai.vectorstore.elasticsearch.index-name:spring-ai-document-index}")
	private String elasticsearchIndexName;

	@Autowired
	private ExecutorService executorService;

	@Autowired
	@Qualifier("simpleVectorStore")
	private VectorStore vectorStore;

	@Autowired
	private FusionStrategy fusionStrategy;

	@Autowired
	private DataAgentProperties dataAgentProperties;

	@Override
	public HybridRetrievalStrategy getObject() throws Exception {

		if (!dataAgentProperties.getVectorStore().isEnableHybridSearch()) {
			log.warn("Hybrid search is disabled. Returning null HybridRetrievalStrategy.");
			return null;
		}
		if ("elasticsearch".equalsIgnoreCase(vectorStoreType)) {
			log.info("Creating ElasticsearchHybridRetrievalStrategy with index: {}", elasticsearchIndexName);
			ElasticsearchHybridRetrievalStrategy strategy = new ElasticsearchHybridRetrievalStrategy(executorService,
					vectorStore, fusionStrategy);
			// 设置索引名称
			strategy.setIndexName(elasticsearchIndexName);
			// 从DataAgentProperties获取最小分数
			strategy.setMinScore(dataAgentProperties.getVectorStore().getElasticsearchMinScore());
			return strategy;
		}
		else {
			log.warn(
					"Creating DefaultHybridRetrievalStrategy (default) without keyword-search ability,maybe you should implement interface -> HybridRetrievalStrategy ");
			PgHyhridRetrievalStrategy strategy = new PgHyhridRetrievalStrategy(executorService, vectorStore, fusionStrategy);
			strategy.setMinScore(dataAgentProperties.getVectorStore().getPgMinScore());
			return strategy;
		}
	}

	@Override
	public Class<?> getObjectType() {
		return HybridRetrievalStrategy.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
