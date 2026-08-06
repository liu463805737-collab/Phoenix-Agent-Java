package com.phoenix.agent.config;

import com.phoenix.data.dto.ModelConfigDTO;
import com.phoenix.data.enums.ModelType;
import com.phoenix.data.service.aimodelconfig.ModelConfigDataService;
import com.zaxxer.hikari.HikariDataSource;
import io.agentscope.core.embedding.EmbeddingModel;
import io.agentscope.core.embedding.dashscope.DashScopeTextEmbedding;
import io.agentscope.core.rag.exception.VectorStoreException;
import io.agentscope.core.rag.knowledge.SimpleKnowledge;
import io.agentscope.core.rag.store.PgVectorStore;
import io.agentscope.core.skill.repository.postgresql.PostgresSkillRepository;
import io.agentscope.extensions.postgresql.state.PostgresAgentStateStore;
import io.agentscope.extensions.redis.RedisDistributedStore;
import io.agentscope.extensions.redis.store.RedisStore;
import io.agentscope.harness.agent.IsolationScope;
import io.agentscope.harness.agent.filesystem.spec.RemoteFilesystemSpec;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.JedisPooled;

import java.time.Duration;

@Configuration
public class HarnessConfig {

    @Bean
    @DependsOn(value = "harnessRedisStore")
    public RemoteFilesystemSpec pgRemoteFilesystemSpec(RedisStore redisStore) {
        return new RemoteFilesystemSpec(redisStore).isolationScope(IsolationScope.USER);
    }

    @Bean
    public RedisDistributedStore distributedStore(DataRedisProperties dataRedisProperties) {
        return RedisDistributedStore.fromJedis(
                this.createJedisPooled(dataRedisProperties), "phoenix:session:");
    }

    private JedisPooled createJedisPooled(DataRedisProperties dataRedisProperties) {
        ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        poolConfig.setMaxTotal(dataRedisProperties.getLettuce().getPool().getMaxActive());
        poolConfig.setMaxIdle(dataRedisProperties.getLettuce().getPool().getMaxIdle());
        poolConfig.setMinIdle(dataRedisProperties.getLettuce().getPool().getMinIdle());
        poolConfig.setMaxWait(dataRedisProperties.getLettuce().getPool().getMaxWait());  // Jedis 4.x 直接支持 Duration
        // 生产环境建议开启
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
        JedisPooled jedisPooled = new JedisPooled(
                poolConfig,           // 连接池配置
                dataRedisProperties.getHost(),                 // 主机
                dataRedisProperties.getPort(),                // 端口
                dataRedisProperties.getTimeout().toSecondsPart(),  // 连接超时（毫秒）
                dataRedisProperties.getPassword(),             // 密码
                dataRedisProperties.getDatabase()        // 数据库索引
        );
        return jedisPooled;
    }

    @Bean(name = "harnessRedisStore")
    public RedisStore redisStore(DataRedisProperties dataRedisProperties) {
        return new RedisStore(this.createJedisPooled(dataRedisProperties), "phoenix:agentscope:store:");
    }


    @Bean
    public PostgresSkillRepository postgresSkillRepository(HikariDataSource hikariDataSource) {
        return PostgresSkillRepository.builder(hikariDataSource)
                .schemaName("public")
                .skillsTableName("tbl_harness_skills")
                .resourcesTableName("tbl_harness_skill_resources")
                .createIfNotExist(true)
                .writeable(true)
                .build();
    }

    /**
     * 配置存放聊天记录
     *
     * @param hikariDataSource
     * @return
     */
    @Bean(name = "harnessPostgresAgentStateStore")
    public PostgresAgentStateStore postgresAgentStateStore(HikariDataSource hikariDataSource) {
        return PostgresAgentStateStore.builder(hikariDataSource)
                .schemaName("public")
                .tableName("tbl_harness_store_state")
                .createIfNotExist(true)
                .build();
    }

    /**
     * 配置存放知识库
     *
     * @param dataSourceProperties
     * @return
     * @throws VectorStoreException
     */
    @Bean(name = "harnessPgVectorStoreKnowledge")
    public PgVectorStore harnessPgVectorStore(DataSourceProperties dataSourceProperties) throws VectorStoreException {
        return PgVectorStore.builder().jdbcUrl(dataSourceProperties.getUrl())
                .username(dataSourceProperties.getUsername())
                .password(dataSourceProperties.getPassword())
                .schema("public")
                .tableName("tbl_harness_vector_store_knowledge")
                .dimensions(512)
                .build();
    }

    /**
     * 配置简单知识库
     *
     * @param harnessPgVectorStore
     * @param modelConfigDataService
     * @return
     */
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
