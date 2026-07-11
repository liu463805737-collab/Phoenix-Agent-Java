package com.phoenix.agent.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户记忆信息表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tbl_agent_user_memory_info")
public class UserMemoryInfo implements Serializable {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String id;
    /** 用户ID */
    private String userId;
    private String agentSn;
    /** 记忆类型 PROFILE-用户画像 FACT-事实记忆 */
    private String memoryType;
    /** 记忆内容 JSON或纯文本 */
    private String content;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
