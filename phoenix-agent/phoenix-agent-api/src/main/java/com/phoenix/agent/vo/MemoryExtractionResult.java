package com.phoenix.agent.vo;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 大模型提取长期记忆后的结构化返回结果
 * 使用 Jackson 注解帮助大模型更好地理解字段含义并生成准确的 JSON
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoryExtractionResult implements Serializable {

    /**
     * 是否包含值得长期保存的记忆
     */
    @JsonPropertyDescription("布尔值，表示该次对话是否包含值得长期保存的记忆。如果仅仅是日常问候或无意义闲聊，则为 false")
    private boolean hasMemory;

    /**
     * 用户画像更新（Key-Value 形式）
     * 例如: {"preferred_language": "中文", "tech_stack": "Java, Spring Boot"}
     */
    @JsonPropertyDescription("用户画像的更新信息。以键值对形式存储用户的偏好、技术栈、工作习惯等。如果没有新偏好，则为 null")
    private Map<String, String> userProfileUpdates;

    /**
     * 提取出的客观事实列表
     * 例如: ["用户当前正在开发一个电商项目", "用户的项目数据库使用的是 MySQL"]
     */
    @JsonPropertyDescription("从对话中提取出的客观事实或业务规则列表。如果没有新事实，则为 null")
    private List<String> facts;

    /**
     * 一句话总结
     * 用于后续存入向量数据库（Vector DB），作为语义检索的索引
     */
    @JsonPropertyDescription("对这次用户交互核心意图的一句话总结。用于未来进行向量相似度检索")
    private String summary;
}
