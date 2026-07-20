package com.phoenix.agent.harness.agent.rules;

import cn.hutool.core.collection.CollUtil;
import com.phoenix.data.dto.search.AgentSearchRequest;
import com.phoenix.data.service.vectorstore.AgentVectorStoreService;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.phoenix.data.constant.DocumentMetadataConstant.AGENT_KNOWLEDGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class RulesRagTool {
    @Autowired
    private AgentVectorStoreService agentVectorStoreService;

    @Tool(name = "getRagInfo", description = "根据用户的自然语言生成的关键词获取向量列表，如果查询到数据就不需要再次调用了",readOnly = true,
    concurrencySafe = true)
    public String getRagInfo(@ToolParam(name = "query", description = "查询向量的关键词") List<String> query) {
//        String[] splits = query.split(" ");
        List<Document> documents = new ArrayList<>();
        for (String split : query) {
            AgentSearchRequest searchRequest = AgentSearchRequest.builder().query(split).docVectorType(AGENT_KNOWLEDGE).agentId(18+"").topK(10).similarityThreshold(0.65).build();
            List<Document> ds = agentVectorStoreService.search(searchRequest);
            documents.addAll(ds);
        }
        List<Document> top5Documents = documents.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Document::getId))),
                        ArrayList::new
                )).stream().sorted(Comparator.comparingDouble(Document::getScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        for (Document document : top5Documents) {
            sb.append(document.getText()).append("\n");
        }
        String result = "";
        if (CollUtil.isNotEmpty(top5Documents)) {
            result = """
                    找到 %s 条相关文档 无需调用tool，直接输出
                    相关文档如下： \n
                    """.formatted(top5Documents.size()) + sb;
        } else {
            result = """
                    没有找到相关文档，请重新理解用户的话
                    """;
        }

        return result;
    }
}
