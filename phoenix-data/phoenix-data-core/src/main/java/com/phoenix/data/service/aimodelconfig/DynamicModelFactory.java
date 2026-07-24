package com.phoenix.data.service.aimodelconfig;

import com.phoenix.data.dto.ModelConfigDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHost;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

/**
 * 动态模型工厂，根据配置创建 ChatModel 和 EmbeddingModel 实例，支持代理和路径自定义。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicModelFactory {

    /**
     * 创建 ChatModel 实例，通过 OpenAiChatModel 和 baseUrl 实现多厂商兼容
     */
    public ChatModel createChatModel(ModelConfigDTO config) {

        log.info("Creating NEW ChatModel instance. Provider: {}, Model: {}, BaseUrl: {}", config.getProvider(),
                config.getModelName(), config.getBaseUrl());
        // 1. 验证参数
        checkBasic(config);

        // 2. 构建 OpenAiApi (核心通讯对象)
        String apiKey = StringUtils.hasText(config.getApiKey()) ? config.getApiKey() : "";
        OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl(config.getBaseUrl())
                .restClientBuilder(getProxiedRestClientBuilder(config))
                .webClientBuilder(getProxiedWebClientBuilder(config));

        if (StringUtils.hasText(config.getCompletionsPath())) {
            apiBuilder.completionsPath(config.getCompletionsPath());
        }
        OpenAiApi openAiApi = apiBuilder.build();

        // 3. 构建运行时选项 (设置默认的模型名称，如 "deepseek-chat" 或 "gpt-4")
        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                .model(config.getModelName())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .streamUsage(true)
                .build();
        // 4. 返回统一的 OpenAiChatModel
        return OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(openAiChatOptions).build();
    }

    /**
     * 创建 EmbeddingModel 实例，支持配置路径和代理
     */
    public EmbeddingModel createEmbeddingModel(ModelConfigDTO config) {
        log.info("Creating NEW EmbeddingModel instance. Provider: {}, Model: {}, BaseUrl: {}", config.getProvider(),
                config.getModelName(), config.getBaseUrl());
        checkBasic(config);

        String apiKey = StringUtils.hasText(config.getApiKey()) ? config.getApiKey() : "";
        OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl(config.getBaseUrl())
                .restClientBuilder(getProxiedRestClientBuilder(config))
                .webClientBuilder(getProxiedWebClientBuilder(config));

        if (StringUtils.hasText(config.getEmbeddingsPath())) {
            apiBuilder.embeddingsPath(config.getEmbeddingsPath());
        }

        OpenAiApi openAiApi = apiBuilder.build();
        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder().model(config.getModelName()).dimensions(512).build(),
                RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    /**
     * 创建 TranscriptionModel 实例，支持配置路径和代理
     */
    public TranscriptionModel createTranscriptionModel(ModelConfigDTO config) {
        log.info("Creating NEW TranscriptionModel instance. Provider: {}, Model: {}, BaseUrl: {}", config.getProvider(),
                config.getModelName(), config.getBaseUrl());
        checkBasic(config);
        String apiKey = StringUtils.hasText(config.getApiKey()) ? config.getApiKey() : "";
        OpenAiAudioApi aiAudioApi = OpenAiAudioApi.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(apiKey)
                .restClientBuilder(getProxiedRestClientBuilder(config))
                .webClientBuilder(getProxiedWebClientBuilder(config))
                .build();
// 1. 手动构建 Options，指定 Qwen 模型
        OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .model(config.getModelName()) // 指定模型
                .language("zh")                         // 指定中文
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .build();
        return new OpenAiAudioTranscriptionModel(aiAudioApi, options);
    }

    /**
     * 校验基本配置参数（baseUrl、apiKey、modelName 不为空）
     */
    private static void checkBasic(ModelConfigDTO config) {
        Assert.hasText(config.getBaseUrl(), "baseUrl must not be empty");
        if (!"custom".equalsIgnoreCase(config.getProvider())) {
            Assert.hasText(config.getApiKey(), "apiKey must not be empty");
        }
        Assert.hasText(config.getModelName(), "modelName must not be empty");
    }

    /**
     * 获取带代理的 RestClient.Builder（同步），支持 Basic 认证
     */
    private RestClient.Builder getProxiedRestClientBuilder(ModelConfigDTO config) {
        if (config.getProxyEnabled() == null || !config.getProxyEnabled()) {
            return RestClient.builder();
        }

        // 打印同步代理日志
        log.info("【Proxy-Init】Model [{}] is using SYNC proxy -> {}:{}", config.getModelName(), config.getProxyHost(),
                config.getProxyPort());

        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        if (StringUtils.hasText(config.getProxyUsername())) {
            log.info("【Proxy-Auth】Enabling Basic Auth for SYNC proxy, user: {}", config.getProxyUsername());
            credsProvider.setCredentials(new AuthScope(config.getProxyHost(), config.getProxyPort()),
                    new UsernamePasswordCredentials(config.getProxyUsername(),
                            config.getProxyPassword().toCharArray()));
        }

        CloseableHttpClient httpClient = HttpClients.custom()
                .setProxy(new HttpHost(config.getProxyHost(), config.getProxyPort()))
                .setDefaultCredentialsProvider(credsProvider)
                .build();

        return RestClient.builder().requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

    /**
     * 获取带代理的 WebClient.Builder（异步 Netty），支持 Basic 认证
     */
    private WebClient.Builder getProxiedWebClientBuilder(ModelConfigDTO config) {
        if (config.getProxyEnabled() == null || !config.getProxyEnabled()) {
            return WebClient.builder();
        }

        log.info("【Proxy-Init】Model [{}] is using ASYNC (Netty) proxy -> {}:{}", config.getModelName(),
                config.getProxyHost(), config.getProxyPort());

        HttpClient nettyClient = HttpClient.create().responseTimeout(java.time.Duration.ofMinutes(3)).proxy(p -> {
            ProxyProvider.Builder proxyBuilder = p.type(ProxyProvider.Proxy.HTTP)
                    .host(config.getProxyHost())
                    .port(config.getProxyPort());

            if (StringUtils.hasText(config.getProxyUsername())) {
                log.info("【Proxy-Auth】Enabling Basic Auth for ASYNC proxy, user: {}", config.getProxyUsername());
                proxyBuilder.username(config.getProxyUsername()).password(s -> config.getProxyPassword());
            }
        });

        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(nettyClient));
    }

}
