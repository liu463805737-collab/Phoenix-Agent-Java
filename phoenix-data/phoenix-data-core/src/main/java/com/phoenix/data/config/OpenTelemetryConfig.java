package com.phoenix.data.config;

import com.phoenix.data.constant.Constant;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * @author zihenzzz
 * @date 2026/2/16 13:55
 */

/**
 * OpenTelemetry 配置类，集成 Langfuse 进行链路追踪。
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = Constant.PROJECT_PROPERTIES_PREFIX + ".langfuse")
public class OpenTelemetryConfig {

	private static final String SERVICE_NAME = "data-agent";

	private boolean enabled = true;

	private String host;

	private String publicKey;

	private String secretKey;

	private SdkTracerProvider tracerProvider;

	/**
	 * 配置 OpenTelemetry SDK，通过 OTLP HTTP 导出器将链路数据发送到 Langfuse。
	 * @return OpenTelemetry 实例，若未启用则返回无操作实现
	 */
	@Bean
	public OpenTelemetry openTelemetry() {
		if (!enabled) {
			return OpenTelemetry.noop();
		}

		String auth = publicKey + ":" + secretKey;
		String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

		OtlpHttpSpanExporter spanExporter = OtlpHttpSpanExporter.builder()
			.setEndpoint(host + "/api/public/otel/v1/traces")
			.addHeader("Authorization", "Basic " + encodedAuth)
			.setTimeout(10, TimeUnit.SECONDS)
			.build();

		Resource resource = Resource.getDefault()
			.merge(Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), SERVICE_NAME)));

		tracerProvider = SdkTracerProvider.builder()
			.addSpanProcessor(BatchSpanProcessor.builder(spanExporter)
				.setScheduleDelay(1, TimeUnit.SECONDS)
				.setMaxExportBatchSize(100)
				.build())
			.setResource(resource)
			.build();

		OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();

		log.info("OpenTelemetry initialized with Langfuse OTLP HTTP exporter");

		return openTelemetrySdk;
	}

	/**
	 * 获取 Langfuse 链路追踪的 Tracer 实例。
	 * @param openTelemetry OpenTelemetry 实例
	 * @return Tracer 实例
	 */
	@Bean
	public Tracer langfuseTracer(OpenTelemetry openTelemetry) {
		return openTelemetry.getTracer(SERVICE_NAME);
	}

	/**
	 * 应用关闭时优雅地关闭 TracerProvider，释放资源。
	 */
	@PreDestroy
	public void shutdown() {
		if (tracerProvider != null) {
			tracerProvider.close();
		}
	}

}
