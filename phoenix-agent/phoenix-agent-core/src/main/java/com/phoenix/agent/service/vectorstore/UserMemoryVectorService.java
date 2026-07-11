package com.phoenix.agent.service.vectorstore;

import com.phoenix.common.vectorstore.request.SearchVectorRequest;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

public interface UserMemoryVectorService {

    List<Document> searchUserMemoryDocumentKnowledge(SearchVectorRequest request);

    void add(String summary, Map<String,Object> metadata);
}
