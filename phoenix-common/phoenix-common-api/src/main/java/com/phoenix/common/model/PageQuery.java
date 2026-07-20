package com.phoenix.common.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public abstract class PageQuery implements Serializable {
    private static final long serialVersionUID = -3263921252635611410L;
    /**
     * 页码,默认为1
     */
    private Integer page = 1;

    /**
     * 页大小,默认为10
     */
    private Integer size = 10;

    /**
     * 搜索关键字-模糊匹配
     */
    private String keyword;

}
