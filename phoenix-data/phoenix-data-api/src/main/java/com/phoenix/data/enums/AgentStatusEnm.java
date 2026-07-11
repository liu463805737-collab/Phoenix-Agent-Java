package com.phoenix.data.enums;

/**
 * 智能体状态枚举
 */
public enum AgentStatusEnm {
    DRAFT("draft", "草稿"), PUBLISHED("published", "已发布"), OFFLINE("offline", "已下线");
    private  String code;
    private  String desc;
    /**
     * 默认构造器
     */
    AgentStatusEnm(){}
    /**
     * 构造智能体状态枚举
     *
     * @param code 状态编码
     * @param desc 状态描述
     */
    AgentStatusEnm(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取状态编码
     *
     * @return 状态编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置状态编码
     *
     * @param code 状态编码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 设置状态描述
     *
     * @param desc 状态描述
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
