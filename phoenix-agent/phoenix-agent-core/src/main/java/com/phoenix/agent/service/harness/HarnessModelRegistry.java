package com.phoenix.agent.service.harness;

import com.phoenix.data.dto.ModelConfigDTO;
import com.phoenix.data.enums.ModelType;
import com.phoenix.data.service.aimodelconfig.ModelConfigDataService;
import io.agentscope.core.embedding.EmbeddingModel;
import io.agentscope.core.embedding.dashscope.DashScopeTextEmbedding;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.model.openai.formatter.OpenAIChatFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI 模型注册中心，管理 ChatClient 和 EmbeddingModel 的懒加载、缓存及热切换。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HarnessModelRegistry {
    private final ModelConfigDataService modelConfigDataService;
    // 缓存对象 (volatile 保证可见性)
    private volatile EmbeddingModel currentEmbeddingModel;
    private volatile OpenAIChatModel currentChatModel;

    /**
     * 获取全局 ChatClient（懒加载 + 双重检查锁缓存）
     */
    public OpenAIChatModel getOpenAIChatModel() {
        if (currentChatModel == null) {
            synchronized (this) {
                if (currentChatModel == null) {
                    log.info("Initializing global ChatClient...");
                    try {
                        ModelConfigDTO config = modelConfigDataService.getActiveConfigByType(ModelType.CHAT);
                        if (config != null) {
                            currentChatModel = OpenAIChatModel.builder()
                                    .apiKey(config.getApiKey())
                                    .modelName(config.getModelName())
                                    .baseUrl(config.getBaseUrl())
                                    .stream(true)
                                    .formatter(new OpenAIChatFormatter())
                                    .build();
                        }
                    } catch (Exception e) {
                        log.error("Failed to initialize ChatClient: {}", e.getMessage(), e);
                    }
                    // 兜底：如果还没初始化成功，抛出运行时异常，提示用户配置
                    if (currentChatModel == null) {
                        throw new RuntimeException(
                                "No active CHAT model configured. Please configure it in the dashboard.");
                    }
                }
            }
        }
        return currentChatModel;
    }

    /**
     * 获取全局 EmbeddingModel（懒加载 + 双重检查锁缓存，无可用模型时使用 Dummy 兜底）
     */
    public EmbeddingModel getEmbeddingModel() {
        if (currentEmbeddingModel == null) {
            synchronized (this) {
                if (currentEmbeddingModel == null) {
                    log.info("Initializing global EmbeddingModel...");
                    try {
                        ModelConfigDTO config = modelConfigDataService.getActiveConfigByType(ModelType.EMBEDDING);
                        if (config != null) {
                            currentEmbeddingModel = DashScopeTextEmbedding.builder()
                                    .apiKey(config.getApiKey())
                                    .modelName(config.getModelName())
                                    .dimensions(512)
                                    .build();
                        }
                    } catch (Exception e) {
                        log.error("Failed to initialize EmbeddingModel: {}", e.getMessage());
                    }
                }
            }
        }
        return currentEmbeddingModel;
    }

    /**
     * 刷新 Chat 缓存（用于热切换）
     */
    public void refreshChat() {
        this.currentChatModel = null;
        log.info("Chat cache cleared.");
    }

    /**
     * 刷新 Embedding 缓存（用于热切换）
     */
    public void refreshEmbedding() {
        this.currentEmbeddingModel = null;
        log.info("Embedding cache cleared.");
    }

}
