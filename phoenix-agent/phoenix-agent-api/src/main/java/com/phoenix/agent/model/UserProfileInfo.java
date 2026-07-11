package com.phoenix.agent.model;


import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户画像信息表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "tbl_agent_user_profile_info")
public class UserProfileInfo implements Serializable {
    /** 用户ID（主键） */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String userId;
    //智能体标识
    private String agentSn;
    /** 画像数据 JSON */
    @Column(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> profileData;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
