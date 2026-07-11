package com.phoenix.agent.service.vectorstore.impl;

import cn.hutool.core.util.StrUtil;
import com.phoenix.agent.service.vectorstore.UserMemoryVectorService;
import com.phoenix.common.vectorstore.request.SearchVectorRequest;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserMemoryVectorServiceImpl implements UserMemoryVectorService {
    @Autowired
    @Qualifier("userMemoryVectorStore")
    private PgVectorStore pgVectorStore;


    @Override
    public void add(String summary, Map<String, Object> metadata) {
        pgVectorStore.add(List.of(new Document(summary, metadata)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> searchUserMemoryDocumentKnowledge(SearchVectorRequest request) {
        if (StrUtil.isNotBlank(request.getQuery())) {
            SearchRequest.Builder builder = SearchRequest.builder().query(request.getQuery()).topK(request.getTopK()).similarityThreshold(request.getThreshold());
            if (request.getFilter() != null) {
                builder.filterExpression(request.getFilter());
            }
            SearchRequest searchRequest = builder.build();
            return pgVectorStore.similaritySearch(searchRequest);
        }
       return List.of();
    }
}
