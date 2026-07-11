package com.phoenix.rag.service;

import com.phoenix.common.vectorstore.request.SearchVectorRequest;
import org.springframework.ai.document.Document;

import java.util.List;

public interface RagService {

    void addDocuments(List<Document> documents) ;

    void deleteDocumentsById(String id) ;

    List<Document> queryDocumentsByRequest(SearchVectorRequest request);
}
