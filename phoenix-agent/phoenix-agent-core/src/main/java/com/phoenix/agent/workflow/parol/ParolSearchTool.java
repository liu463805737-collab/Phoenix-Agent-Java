package com.phoenix.agent.workflow.parol;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Component
@RequiredArgsConstructor
public class ParolSearchTool {
//    private final PatrolDocVectorStore businessDocVectorStore;

    /**
     * 搜索业务文档
     * 根据关键词和用户负责区域，在MongoDB向量数据库中查找最相关的文档
     *
     * @param searchQuery     搜索关键词或问题描述
     * @return 格式化的相关文档列表（标题+摘要+相似度）
     */
    @Tool(name = "searchParolDocuments", description = "搜索与用户负责区域相关的业务文档。当用户询问业务规范、操作流程、规章制度等文档内容时调用此工具。根据搜索关键词和用户负责区域在向量数据库中检索最相关的文档。")
    public String searchParolDocuments(
            @ToolParam(description = "搜索关键词或问题描述，例如：巡逻流程规范、应急处置流程") String searchQuery,
            @ToolParam(description = "用户负责的区域名称，例如：瀛海镇、故宫、亦庄镇") String responsibleArea
    ) {
//
        log.info("AI调用文档搜索工具: query={}, responsibleArea={}", searchQuery, responsibleArea);
        List<Document> documents = new ArrayList<>();
//        List<Document> documents = businessDocVectorStore.searchBusinessDocumentKnowledge(searchQuery, responsibleArea);
        if (documents == null || documents.isEmpty()) {
            return "未找到与\"" + searchQuery + "\"相关的文档（区域：）";
        }

        StringBuilder sb = new StringBuilder();
        for (Document document : documents) {
            sb.append(String.format(
                    "【%s】(相似度: %.2f)\n分类: %s\n摘要: %s",
                    document.getMetadata().get("title"), document.getScore(),
                    document.getMetadata().get("category") != null ? document.getMetadata().get("category") : "未分类",
                    document.getText()
            )).append("\n---\n");
        }
        log.info("文档搜索返回{}条结果", documents.size());
        return "找到" + documents.size() + "条相关文档：\n" + sb.toString();
    }
}
