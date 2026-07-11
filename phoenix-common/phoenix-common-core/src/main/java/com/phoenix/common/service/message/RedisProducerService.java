package com.phoenix.common.service.message;

import java.util.List;

public interface RedisProducerService<T> {
    /**
     * 批量发送消息
     * @param objs
     */
    void publishBatch(String queue, List<T> objs) ;

    /**
     * 批量发送消息
     * @param obj
     */
    void publishOne(String queue, T obj) ;
}
