package com.phoenix.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 基本实体类
 */
@Data
public abstract class BaseModel implements Serializable {
    /**
     * 创建时间
     **/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(value = "create_time", onInsertValue = "now()")
    protected Date createTime;
    /**
     * 创建人
     **/
    @Column("creator")
    protected String creator;
    /**
     * 更新时间
     **/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(value = "update_time", onUpdateValue = "now()")
    protected Date updateTime;
    /**
     * 更新人
     **/
    @Column("updator")
    protected String updator;
    /**
     * 删除标识 0标识未删除 1标识删除
     **/
    @Column("del_flag")
    protected Integer delFlag = 0;
    /**
     * 查询关键字
     * 临时变量 用于查询
     */
    @Column(ignore = true)
    protected String keyword;

}
