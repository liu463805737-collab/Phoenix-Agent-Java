package com.phoenix.rag.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.phoenix.common.vectorstore.request.SearchVectorRequest;
import com.phoenix.rag.service.RagService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class RagServiceImpl implements RagService {
    @Autowired
    @Qualifier("ragVectorStore")
    private PgVectorStore pgVectorStore;

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollUtil.isNotEmpty(documents)) {
            List<List<Document>> lists = ListUtil.partition(documents, 50);
            for (List<Document> list : lists) {
                pgVectorStore.doAdd(list);
            }
        }
    }

    @Override
    public void deleteDocumentsById(String id) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        Filter.Expression filterExpression = builder.eq("ragId", id).build();
        pgVectorStore.delete(filterExpression);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> queryDocumentsByRequest(SearchVectorRequest request) {
        SearchRequest.Builder builder = SearchRequest.builder().query(request.getQuery()).topK(request.getTopK()).similarityThreshold(request.getThreshold());
        if (request.getFilter() != null) {
            builder.filterExpression(request.getFilter());
        }
        SearchRequest searchRequest = builder.build();
        return pgVectorStore.similaritySearch(searchRequest);
    }
}
