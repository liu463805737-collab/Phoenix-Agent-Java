package com.phoenix.platform.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("spring.ai.phoenix.platform")
public class PhoenixPlatformProperties {
}


