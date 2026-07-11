package com.phoenix.privilege.config;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.phoenix.privilege.enums.AuthErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * satoken配置
 */
@Slf4j
@Configuration
public class SaTokenConfigure implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 在 WebFlux 下调用 Sa-Token 同步 API 需要先 set 上下文
        SaReactorSyncHolder.setContext(exchange);
        return chain.filter(exchange)
                .doFinally(signalType -> SaReactorSyncHolder.clearContext());
    }

    @Bean
    @Primary
    public SaTokenConfig saTokenConfig() {
        SaTokenConfig config = new SaTokenConfig();
        config.setTokenName("phoenix-token");
        config.setTimeout(7 * 24 * 60 * 60);
        config.setActiveTimeout(-1);
        config.setIsConcurrent(true);
        config.setIsShare(true);
        config.setTokenStyle("tik");
        config.setIsLog(false);
        config.setIsReadHeader(true);
        config.setIsReadCookie(false);
        config.setIsReadBody(false);
        return config;
    }

    /**
     * 注册 [sa-token全局过滤器]
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                .addInclude("/api/**")
                .addExclude("favicon.ico", "/api/privilege/auth/*", "/api/auth/*")
                .setAuth(r -> StpUtil.checkLogin())
                .setError(e -> {
                    log.error("---------- sa全局异常 ", e);
                    return new SaResult(AuthErrorCode.TOKEN_INVALID.getCode(), AuthErrorCode.TOKEN_INVALID.getMessage(), null);
                })
                ;
    }

}
