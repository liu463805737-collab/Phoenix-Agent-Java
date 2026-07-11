package com.phoenix.rag.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.phoenix.common.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "tbl_rag_file_info")
public class RagFileInfo extends BaseModel {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String id;
    // pdf word md
    private String fileType;
    private String categoryId;
    private String name;
    private String title;
    private String description;
    private String path;
    // 1为按页生成docment 2按照段落生成docment
    private String pdfType;
    private Integer pageTopMargin;
    private Integer pagesPerDocument;
    private boolean textSplitter;
}
