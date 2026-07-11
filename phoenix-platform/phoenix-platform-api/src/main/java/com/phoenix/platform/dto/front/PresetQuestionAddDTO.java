package com.phoenix.platform.dto.front;

import lombok.Data;

/**
 * 预设问题添加DTO
 */
@Data
public class PresetQuestionAddDTO {

    /**
     * 智能体ID
     */
    private Long agentId;
    /**
     * 问题内容
     */
    private String question;

    /**
     * 排序顺序
     */
    private Integer sortOrder;
}
