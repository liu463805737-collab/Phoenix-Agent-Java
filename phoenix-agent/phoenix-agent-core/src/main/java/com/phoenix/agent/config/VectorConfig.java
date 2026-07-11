package com.phoenix.agent.config;

import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.summarization.SummarizationHook;
import com.phoenix.data.service.aimodelconfig.AiModelRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
@RequiredArgsConstructor
public class VectorConfig {
    private final JdbcTemplate jdbcTemplate;
    private final AiModelRegistry aiModelRegistry;


    /**
     * 用户记忆向量
     * @return
     */
    @Bean
    @Qualifier("userMemoryVectorStore")
    public PgVectorStore userMemoryVectorStore() {
        return PgVectorStore.builder(jdbcTemplate, aiModelRegistry.getEmbeddingModel())
                .dimensions(512)                    // 向量维度（需与 Embedding 模型匹配）
                .distanceType(COSINE_DISTANCE)       // 距离度量策略（余弦相似度）
                .indexType(HNSW)                     // 索引类型（HNSW 或 IVFFlat）
                .initializeSchema(true)              // 启动时自动创建表和索引
                .vectorTableName("tbl_vector_store_user_memory")   // 自定义表名
                .maxDocumentBatchSize(200)         // 批量写入的上限
                .build();
    }

    /**
     * RAG的向量
     * @return
     */
    @Bean
    @Qualifier("ragVectorStore")
    public PgVectorStore ragVectorStore() {
        return PgVectorStore.builder(jdbcTemplate, aiModelRegistry.getEmbeddingModel())
                .dimensions(512)                    // 向量维度（需与 Embedding 模型匹配）
                .distanceType(COSINE_DISTANCE)       // 距离度量策略（余弦相似度）
                .indexType(HNSW)                     // 索引类型（HNSW 或 IVFFlat）
                .initializeSchema(true)              // 启动时自动创建表和索引
                .vectorTableName("tbl_vector_store_rag")   // 自定义表名
                .maxDocumentBatchSize(200)         // 批量写入的上限
                .build();
    }
}
