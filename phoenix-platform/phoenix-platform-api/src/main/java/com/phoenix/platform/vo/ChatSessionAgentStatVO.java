package com.phoenix.platform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 智能体维度请求统计 VO
 *
 * <p>按智能体聚合：该智能体被多少个用户使用、总共产生多少请求、最后一次请求时间。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionAgentStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 智能体ID */
    private Integer agentId;

    /** 智能体名称 */
    private String agentName;

    /** 使用人数（去重 userId） */
    private Long userCount;

    /** 请求总数（会话数） */
    private Long requestCount;

    /** 最后请求时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastRequestTime;
}