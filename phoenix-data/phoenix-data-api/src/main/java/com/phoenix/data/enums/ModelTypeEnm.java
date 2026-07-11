package com.phoenix.data.enums;

/**
 * 模型类型
 */
public enum ModelTypeEnm {
    CHAT("CHAT", "聊天模型"), EMBEDDING("EMBEDDING", "嵌入式模型");
    private  String code;
    private  String desc;
    /**
     * 默认构造器
     */
    ModelTypeEnm(){}
    /**
     * 构造模型类型枚举
     *
     * @param code 类型编码
     * @param desc 类型描述
     */
    ModelTypeEnm(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取模型类型编码
     *
     * @return 类型编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置模型类型编码
     *
     * @param code 类型编码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取模型类型描述
     *
     * @return 类型描述
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 设置模型类型描述
     *
     * @param desc 类型描述
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
