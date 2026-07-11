package com.phoenix.rag.service;

import com.mybatisflex.core.service.IService;
import com.phoenix.rag.model.RagFileInfo;
import com.phoenix.rag.vo.RagFileInfoVo;
import org.springframework.ai.document.Document;

import java.util.List;

public interface RagFileInfoService extends IService<RagFileInfo> {

    List<Document> addDocuments(RagFileInfoVo fileInfoVo);
}
