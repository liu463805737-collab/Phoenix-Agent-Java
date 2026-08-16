package com.phoenix.platform.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 用户对智能体的请求记录查询参数
 */
@Data
public class ChatSessionRequestQuery {

    /** 当前页，从1开始 */
    @Min(1)
    private Integer pageNum = 1;

    /** 每页条数 */
    @Min(1)
    @Max(500)
    private Integer pageSize = 10;

    /** 智能体ID */
    private Integer agentId;

    /** 用户ID */
    private String userId;

    /** 会话标题关键字 */
    private String title;

    /** 消息角色：user/assistant */
    private String role;

    /** 消息类型 */
    private String messageType;

    /** 内容关键字 */
    private String content;

    /** 用户账号/姓名关键字 */
    private String keyword;

    /** 开始时间 yyyy-MM-dd HH:mm:ss */
    private String startTime;

    /** 结束时间 yyyy-MM-dd HH:mm:ss */
    private String endTime;

    /** 排序字段：create_time / agent_id / user_id */
    private String orderBy = "create_time";

    /** 排序方向：desc/asc */
    private String orderDir = "desc";
}