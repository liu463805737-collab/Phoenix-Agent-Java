package com.phoenix.common.vectorstore.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.ai.vectorstore.filter.Filter;

import java.io.Serializable;

@Data
@Builder
public class SearchVectorRequest implements Serializable {
    private String query;
    private Filter.Expression filter;
    private int topK = 20;
    /**
     * 必须小于1
     */
    private double threshold = 0.5d;
}
