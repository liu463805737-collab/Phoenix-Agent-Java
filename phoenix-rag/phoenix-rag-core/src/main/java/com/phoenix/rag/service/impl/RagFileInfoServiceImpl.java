package com.phoenix.rag.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.rag.mapper.RagFileInfoMapper;
import com.phoenix.rag.model.RagFileInfo;
import com.phoenix.rag.service.RagFileInfoService;
import com.phoenix.rag.vo.RagFileInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class RagFileInfoServiceImpl extends ServiceImpl<RagFileInfoMapper, RagFileInfo> implements RagFileInfoService {
    @Override
    public List<Document> addDocuments(RagFileInfoVo fileInfoVo) {
        List<Document> documents = switch (fileInfoVo.getFileType()) {
            case "pdf" -> pdfDocuments(fileInfoVo);
            case "word" -> wordDocuments(fileInfoVo);
            case "md" -> mdDocuments(fileInfoVo);
            default -> null;
        };
        return documents;
    }

    private List<Document> mdDocuments(RagFileInfoVo fileInfoVo) {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true) // 遇到分隔线（---）则创建新文档 [citation:6]
                .withIncludeCodeBlock(true)            // 将代码块包含在文档中 [citation:6]
                .withIncludeBlockquote(true)           // 将引用块包含在文档中 [citation:6]
                .withAdditionalMetadata("filename", fileInfoVo.getName()) // 记录文件来源
                .build();
        MarkdownDocumentReader reader = new MarkdownDocumentReader(fileInfoVo.getResource(), config);
        List<Document> documents = reader.read();
        for (Document document : documents) {
            this.setMetadata(document, fileInfoVo);
        }
        List<Document> chunks = null;
        if (fileInfoVo.isTextSplitter()){
            TokenTextSplitter textSplitter = new TokenTextSplitter();
            chunks = textSplitter.apply(documents);
        }
        return chunks;
    }

    private List<Document> wordDocuments(RagFileInfoVo fileInfoVo) {
        TikaDocumentReader reader = new TikaDocumentReader(fileInfoVo.getResource());
        List<Document> documents = reader.read();
        if (CollUtil.isNotEmpty(documents)) {
            for (Document document : documents) {
                this.setMetadata(document, fileInfoVo);
            }
        }
        return documents;
    }


    private List<Document> pdfDocuments(RagFileInfoVo fileInfoVo) {
        List<Document> documents = new ArrayList<>();
        if ("1".equals(fileInfoVo.getPdfType())){
            PagePdfDocumentReader pageReader = new PagePdfDocumentReader(
                    fileInfoVo.getResource(),
                    PdfDocumentReaderConfig.builder()
                            .withPageTopMargin(fileInfoVo.getPageTopMargin())
                            .withPagesPerDocument(fileInfoVo.getPagesPerDocument())   // 每页生成一个 Document
                            .build()
            );
            documents = pageReader.read();
        }else if ("2".equals(fileInfoVo.getPdfType())){
            // 3. 使用 ParagraphPdfDocumentReader 按段落读取（需要 PDF 包含目录结构）
            ParagraphPdfDocumentReader paragraphReader = new ParagraphPdfDocumentReader(
                    fileInfoVo.getResource(),
                    PdfDocumentReaderConfig.builder()
                            .withPageTopMargin(fileInfoVo.getPageTopMargin())
                            .build()
            );
            documents = paragraphReader.read();
        }
        if (CollUtil.isNotEmpty(documents)) {
            for (Document document : documents) {
                this.setMetadata(document, fileInfoVo);
            }
        }
        List<Document> chunks = null;
        if (fileInfoVo.isTextSplitter()){
            TokenTextSplitter textSplitter = new TokenTextSplitter();
            chunks = textSplitter.apply(documents);
        }
        return chunks;
    }

    private void setMetadata(Document document,RagFileInfoVo fileInfoVo) {
        document.getMetadata().put("ragId", fileInfoVo.getId());
        document.getMetadata().put("name", fileInfoVo.getName());
        document.getMetadata().put("fileType", fileInfoVo.getFileType());
        document.getMetadata().put("title", fileInfoVo.getTitle());
        document.getMetadata().put("description", fileInfoVo.getDescription());
        document.getMetadata().put("path", fileInfoVo.getPath());
    }
}
