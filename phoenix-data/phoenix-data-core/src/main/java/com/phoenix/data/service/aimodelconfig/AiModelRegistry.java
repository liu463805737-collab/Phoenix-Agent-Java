package com.phoenix.data.service.aimodelconfig;

import com.phoenix.data.dto.ModelConfigDTO;
import com.phoenix.data.enums.ModelType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 模型注册中心，管理 ChatClient 和 EmbeddingModel 的懒加载、缓存及热切换。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiModelRegistry {

    private final DynamicModelFactory modelFactory;

    private final ModelConfigDataService modelConfigDataService;

    // 缓存对象 (volatile 保证可见性)
    private volatile ChatClient currentChatClient;

    private volatile EmbeddingModel currentEmbeddingModel;

    /**
     * 获取全局 ChatClient（懒加载 + 双重检查锁缓存）
     */
    public ChatClient getChatClient() {
        if (currentChatClient == null) {
            synchronized (this) {
                if (currentChatClient == null) {
                    log.info("Initializing global ChatClient...");
                    try {
                        ModelConfigDTO config = modelConfigDataService.getActiveConfigByType(ModelType.CHAT);
                        if (config != null) {
                            ChatModel chatModel = modelFactory.createChatModel(config);
                            // 核心：基于新 Model 创建新 Client，彻底消除旧参数缓存
                            currentChatClient = ChatClient.builder(chatModel).build();
                        }
                    } catch (Exception e) {
                        log.error("Failed to initialize ChatClient: {}", e.getMessage(), e);
                    }

                    // 兜底：如果还没初始化成功，抛出运行时异常，提示用户配置
                    if (currentChatClient == null) {
                        throw new RuntimeException(
                                "No active CHAT model configured. Please configure it in the dashboard.");
                    }
                }
            }
        }
        return currentChatClient;
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
                            currentEmbeddingModel = modelFactory.createEmbeddingModel(config);
                        }
                    } catch (Exception e) {
                        log.error("Failed to initialize EmbeddingModel: {}", e.getMessage());
                    }

                    // 兜底：为了防止 VectorStore Starter 启动时调用 dimensions() 报错
                    // 我们必须返回一个"哑巴"模型，而不是 null 或 抛异常
                    if (currentEmbeddingModel == null) {
                        log.warn("Using DummyEmbeddingModel for fallback.");
                        currentEmbeddingModel = new DummyEmbeddingModel();
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
        this.currentChatClient = null;
        log.info("Chat cache cleared.");
    }

    /**
     * 刷新 Embedding 缓存（用于热切换）
     */
    public void refreshEmbedding() {
        this.currentEmbeddingModel = null;
        log.info("Embedding cache cleared.");
    }

    /**
     * 哑巴嵌入模型，仅用于启动时防止 VectorStore 初始化报错
     */
    private static class DummyEmbeddingModel implements EmbeddingModel {

        /**
         * 调用 Embedding 请求，直接抛出异常提示未配置模型
         */
        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            throw new RuntimeException("No active EMBEDDING model. Please configure it first!");
        }

        /**
         * 对 Document 进行嵌入，返回空数组
         */
        @Override
        public float[] embed(Document document) {
            return new float[0];
        }

        /**
         * 对文本进行嵌入，返回空数组
         */
        @Override
        public float[] embed(String text) {
            return new float[0];
        }

        /**
         * 对文本列表进行嵌入，返回空列表
         */
        @Override
        public List<float[]> embed(List<String> texts) {
            return List.of();
        }

        /**
         * 对文本列表进行嵌入并返回响应，返回 null
         */
        @Override
        public EmbeddingResponse embedForResponse(List<String> texts) {
            return null;
        }

        /**
         * 返回固定维度 1536（OpenAI 默认维度），通过向量库的初始化检查
         */
        @Override
        public int dimensions() {
            return 1536;
        }

    }

}
