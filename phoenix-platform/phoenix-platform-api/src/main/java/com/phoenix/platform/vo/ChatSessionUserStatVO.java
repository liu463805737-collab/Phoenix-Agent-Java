package com.phoenix.platform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户维度请求统计 VO
 *
 * <p>按用户聚合：某用户使用了哪些智能体、共产生多少请求、最后一次请求时间。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionUserStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private String userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 涉及智能体数（去重 agentId） */
    private Long agentCount;

    /** 请求总数（会话数） */
    private Long requestCount;

    /** 最后请求时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastRequestTime;
}