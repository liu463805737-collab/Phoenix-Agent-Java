package com.phoenix.common.service.message.impl;

import com.alibaba.fastjson2.JSON;
import com.phoenix.common.service.message.RedisProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisProducerServiceImpl<T> implements RedisProducerService<T> {
    private final StringRedisTemplate redisTemplate;

    @Override
    public void publishBatch(String queue,List<T> objs) {
        String datas = JSON.toJSONString(objs);
        redisTemplate.opsForList().leftPush(queue, datas);
    }

    @Override
    public void publishOne(String queue, T obj) {
        String data = JSON.toJSONString(obj);
        redisTemplate.opsForList().leftPush(queue, data);
    }
}
