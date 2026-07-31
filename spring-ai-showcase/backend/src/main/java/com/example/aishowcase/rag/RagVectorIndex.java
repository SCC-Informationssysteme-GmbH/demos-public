package com.example.aishowcase.rag;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class RagVectorIndex {

    private final EmbeddingService embeddingService;
    private final RagKnowledgeBase knowledgeBase;
    private volatile List<IndexedDocument> index;

    public RagVectorIndex(EmbeddingService embeddingService, RagKnowledgeBase knowledgeBase) {
        this.embeddingService = embeddingService;
        this.knowledgeBase = knowledgeBase;
    }

    public List<RagDocument> search(String query, int topK) {
        List<Double> queryEmbedding = embeddingService.embed(query);
        return ensureIndexed().stream()
                .sorted(Comparator.comparingDouble(
                        (IndexedDocument indexed) -> cosineSimilarity(indexed.embedding(), queryEmbedding)).reversed())
                .limit(topK)
                .map(IndexedDocument::document)
                .toList();
    }

    private synchronized List<IndexedDocument> ensureIndexed() {
        if (index == null) {
            index = knowledgeBase.all().stream()
                    .map(document -> new IndexedDocument(document, embeddingService.embed(document.content())))
                    .toList();
        }
        return index;
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        double dotProduct = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < a.size(); i++) {
            dotProduct += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record IndexedDocument(RagDocument document, List<Double> embedding) {
    }
}
