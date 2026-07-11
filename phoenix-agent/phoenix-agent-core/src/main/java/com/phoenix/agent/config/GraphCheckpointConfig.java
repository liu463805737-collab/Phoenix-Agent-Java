package com.phoenix.agent.config;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.serializer.plain_text.jackson.SpringAIJacksonStateSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.concurrent.TimeUnit;

/**
 * 检测点配置
 */
@Configuration
public class GraphCheckpointConfig {

    /**
     * 存放短期记忆
     *
//     * @param dataSource dataSource
     * @return
     */
//    @Bean
//    @DependsOn("springAIJacksonStateSerializer")
//    public PostgresSaver postgresSaver(DataSource dataSource, SpringAIJacksonStateSerializer springAIJacksonStateSerializer) {
//        return PostgresSaver.builder().datasource(dataSource).stateSerializer(springAIJacksonStateSerializer).createOption(CreateOption.CREATE_IF_NOT_EXISTS).build();
//    }

    @Bean
    public SpringAIJacksonStateSerializer springAIJacksonStateSerializer() {
        // 1. 先创建默认的 serializer（这一步会初始化框架内部安全配置好的 ObjectMapper）
        SpringAIJacksonStateSerializer stateSerializer = new SpringAIJacksonStateSerializer(OverAllState::new);
        // 2. 获取框架内部已经配置好的 ObjectMapper（避免类型信息丢失）
        ObjectMapper mapper = stateSerializer.objectMapper();
        // 3. （可选）如果你需要自定义配置，可以在这里安全地追加，例如处理时间格式
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return stateSerializer;
    }

    @Bean
    @DependsOn("springAIJacksonStateSerializer")
    public RedisSaver redisSaver(RedissonClient redissonClient, SpringAIJacksonStateSerializer springAIJacksonStateSerializer) {
        return RedisSaver.builder().redisson(redissonClient).stateSerializer(springAIJacksonStateSerializer).build();
    }

    @Bean
    public SaverConfig saverConfig(RedisSaver redisSaver) {
        return SaverConfig.builder().register(redisSaver).build();
    }
}
