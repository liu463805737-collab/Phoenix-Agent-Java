package com.phoenix.rag.vo;

import lombok.Data;
import org.springframework.core.io.Resource;

import java.io.Serializable;

@Data
public class RagFileInfoVo implements Serializable {
    private String id;
    private String fileType;
    private String name;
    private String title;
    private String description;
    private String path;
    private Resource resource;
    // 1为按页生成docment 2按照段落生成docment
    private String pdfType;
    private Integer pageTopMargin;
    private Integer pagesPerDocument;
    private boolean textSplitter;
}
