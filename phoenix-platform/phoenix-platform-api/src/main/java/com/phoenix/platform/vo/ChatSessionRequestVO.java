package com.phoenix.platform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户对智能体的请求记录 VO
 *
 * <p>由 tbl_data_chat_session、tbl_data_chat_message、tbl_platform_account_info
 * 关联查询得出，一条记录对应会话中的一条消息。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionRequestVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private String sessionId;

    /** 智能体ID */
    private Integer agentId;

    /** 智能体名称 */
    private String agentName;

    /** 会话标题 */
    private String title;

    /** 用户ID */
    private String userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 消息角色：user-用户，assistant-助手 */
    private String role;

    /** 消息内容 */
    private String content;

    /** 消息类型：text-文本，sql-SQL查询，result-查询结果，error-错误 */
    private String messageType;

    /** 消息创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}