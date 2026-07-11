package com.phoenix.data.config;

import com.phoenix.data.properties.FileStorageProperties;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.nio.file.Paths;
import java.time.Duration;

/**
 * Web配置类 (WebFlux 版本)
 */
@Configuration
@AllArgsConstructor
public class WebConfig implements WebFluxConfigurer {

	private final FileStorageProperties fileStorageProperties;

	/**
	 * 配置静态资源处理器，将文件存储路径映射为 URL 访问路径并设置缓存策略。
	 * @param registry 资源处理器注册表
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String uploadDir = Paths.get(fileStorageProperties.getPath()).toAbsolutePath().toString();

		registry.addResourceHandler(fileStorageProperties.getUrlPrefix() + "/**")
			.addResourceLocations("file:" + uploadDir + "/")
			.setCacheControl(CacheControl.maxAge(Duration.ofHours(1)));
	}

}
