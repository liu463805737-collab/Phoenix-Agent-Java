package com.phoenix.agent.config;

import com.phoenix.data.dto.ModelConfigDTO;
import com.phoenix.data.enums.ModelType;
import com.phoenix.data.service.aimodelconfig.ModelConfigDataService;
import io.agentscope.core.embedding.EmbeddingModel;
import io.agentscope.core.embedding.dashscope.DashScopeTextEmbedding;
import io.agentscope.core.rag.exception.VectorStoreException;
import io.agentscope.core.rag.knowledge.SimpleKnowledge;
import io.agentscope.core.rag.store.PgVectorStore;
import io.agentscope.extensions.redis.RedisDistributedStore;
import io.agentscope.harness.agent.DistributedStore;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import redis.clients.jedis.JedisPooled;

@Configuration
public class HarnessConfig {

    @Bean
    public DistributedStore distributedStore(DataRedisProperties dataRedisProperties) {
        String url = "redis://" + dataRedisProperties.getHost() + ":" + dataRedisProperties.getPort();
        return RedisDistributedStore.fromJedis(
                new JedisPooled(url), "phoenix:session:");
    }

    @Bean(name = "harnessPgVectorStoreKnowledge")
    public PgVectorStore harnessPgVectorStore(DataSourceProperties dataSourceProperties) throws VectorStoreException {
        return PgVectorStore.builder().jdbcUrl(dataSourceProperties.getUrl())
                .username(dataSourceProperties.getUsername())
                .password(dataSourceProperties.getPassword())
                .schema("public")
                .tableName("tbl_vector_store_harness_knowledge")
                .dimensions(512)
                .build();
    }

    @Bean
    @DependsOn(value = "harnessPgVectorStoreKnowledge")
    public SimpleKnowledge simpleKnowledge(PgVectorStore harnessPgVectorStore, ModelConfigDataService modelConfigDataService) {
        ModelConfigDTO config = modelConfigDataService.getActiveConfigByType(ModelType.EMBEDDING);
        EmbeddingModel embeddings = DashScopeTextEmbedding.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .dimensions(512)
                .build();
        return SimpleKnowledge.builder()
                .embeddingModel(embeddings)
                .embeddingStore(harnessPgVectorStore)
                .build();
    }
}
