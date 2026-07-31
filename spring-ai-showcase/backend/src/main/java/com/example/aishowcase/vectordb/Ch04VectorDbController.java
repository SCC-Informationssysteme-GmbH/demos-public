package com.example.aishowcase.vectordb;

import com.example.aishowcase.common.ChannelStatusResponse;
import com.example.aishowcase.rag.RagDocument;
import com.example.aishowcase.rag.RagKnowledgeBase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ch04")
public class Ch04VectorDbController {

    private final RagKnowledgeBase knowledgeBase;
    private final QdrantVectorStoreService vectorStoreService;

    public Ch04VectorDbController(RagKnowledgeBase knowledgeBase, QdrantVectorStoreService vectorStoreService) {
        this.knowledgeBase = knowledgeBase;
        this.vectorStoreService = vectorStoreService;
    }

    @GetMapping("/status")
    public ChannelStatusResponse status() {
        return new ChannelStatusResponse("CH.04", "vectordb", "Vektordatenbank-Integration bereit");
    }

    @PostMapping("/index")
    public VectorDbIndexResponse index() {
        int indexedCount = vectorStoreService.indexDocuments(knowledgeBase.all());
        return new VectorDbIndexResponse("CH.04", "vectordb", indexedCount);
    }

    @PostMapping("/search")
    public VectorDbSearchResponse search(@RequestBody VectorDbSearchRequest request) {
        var matches = vectorStoreService.search(request.query(), 3);
        return new VectorDbSearchResponse("CH.04", "vectordb", request.query(), matches);
    }

    @PostMapping("/documents")
    public DocumentUploadResponse upload(@RequestBody DocumentUploadRequest request) {
        RagDocument document = new RagDocument(request.id(), request.title(), request.content());
        vectorStoreService.indexDocuments(List.of(document));
        return new DocumentUploadResponse("CH.04", "vectordb", document.id(), document.title());
    }
}
