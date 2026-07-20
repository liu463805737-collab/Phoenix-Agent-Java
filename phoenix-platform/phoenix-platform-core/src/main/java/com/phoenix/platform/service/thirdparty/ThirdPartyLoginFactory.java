package com.phoenix.platform.service.thirdparty;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThirdPartyLoginFactory {

    private final List<ThirdPartyLoginStrategy> strategies;

    private final Map<String, ThirdPartyLoginStrategy> strategyMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (ThirdPartyLoginStrategy strategy : strategies) {
            strategyMap.put(strategy.getPlatformType(), strategy);
        }
        log.info("三方登录策略注册完成: {}", strategyMap.keySet());
    }

    public ThirdPartyLoginStrategy getStrategy(String platformType) {
        ThirdPartyLoginStrategy strategy = strategyMap.get(platformType);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的平台类型: " + platformType + "，支持: " + strategyMap.keySet());
        }
        return strategy;
    }
}
