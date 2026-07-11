package com.phoenix.agent.service.impl;

import cn.hutool.core.util.StrUtil;
import com.phoenix.agent.model.UserMemoryInfo;
import com.phoenix.agent.service.MemoryPipelineService;
import com.phoenix.agent.service.UserMemoryInfoService;
import com.phoenix.agent.service.UserProfileInfoService;
import com.phoenix.agent.service.vectorstore.UserMemoryVectorService;
import com.phoenix.agent.vo.MemoryExtractionResult;
import com.phoenix.data.dto.ModelConfigDTO;
import com.phoenix.data.enums.ModelType;
import com.phoenix.data.service.aimodelconfig.DynamicModelFactory;
import com.phoenix.data.service.aimodelconfig.ModelConfigDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class MemoryPipelineServiceImpl implements MemoryPipelineService {
    @Autowired
    private UserMemoryInfoService userMemoryInfoService;
    @Autowired
    private UserProfileInfoService userProfileInfoService;
    @Autowired
    private UserMemoryVectorService userMemoryVectorService;
    @Autowired
    private ModelConfigDataService modelConfigDataService;
    @Autowired
    private DynamicModelFactory modelFactory;

    @Async
    @Override
    public void processAndExtractMemory(String userId, String agentSn, String userQuery) {
        try {
            ModelConfigDTO config = modelConfigDataService.getActiveConfigByType(ModelType.CHAT);
            ChatModel chatModel = modelFactory.createChatModel(config);
            ChatClient chatClient = ChatClient.builder(chatModel).build();
            // 1. 调用大模型提取记忆
            String promptText = buildExtractionPrompt(userQuery);


            // 使用 Spring AI 获取结构化响应
            MemoryExtractionResult result = chatClient.prompt().user(promptText).call().entity(MemoryExtractionResult.class); // 自动反序列化为对象

            // 2. 判断是否有有效记忆
            if (result == null || !result.isHasMemory()) {
                return;
            }

            // 3. 持久化用户画像更新 (覆盖或合并逻辑)
            if (result.getUserProfileUpdates() != null) {
                userProfileInfoService.saveProfileUpdates(userId, agentSn, result.getUserProfileUpdates());
            }
            LocalDateTime now = LocalDateTime.now();
            // 4. 持久化事实/摘要 (可结合 Spring AI 的 VectorStore 存入向量库)
            if (result.getFacts() != null) {
                for (String fact : result.getFacts()) {
                    UserMemoryInfo userMemoryInfo = UserMemoryInfo.builder().userId(userId).agentSn(agentSn).memoryType("FACT").content(fact).createdAt(now).build();
                    userMemoryInfoService.save(userMemoryInfo);
                }
            }
            // 5. 将 summary 写入 VectorStore (如阿里云 AnalyticDB / Milvus)
            long createdAt = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            if (StrUtil.isNotBlank(result.getSummary())) {
                userMemoryVectorService.add(result.getSummary(), Map.of("userId", userId, "agentSn", agentSn, "createdAt", createdAt));
            }
        } catch (Exception e) {
            // 记忆提取失败不应影响主流程，记录日志即可
            log.error("Memory extraction failed for user: {}", userId, e);
        }
    }

    // 构建 Prompt 的辅助方法
    private String buildExtractionPrompt(String query) {
        return "你是一个智能体的记忆提取引擎...(此处替换为上面的完整Prompt)\n\n# User Input\n" + query;
    }
}
