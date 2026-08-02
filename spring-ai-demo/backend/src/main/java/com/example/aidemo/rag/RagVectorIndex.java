package com.example.aidemo.rag;

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
        float[] queryEmbedding = embeddingService.embed(query);
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

    private double cosineSimilarity(float[] a, float[] b) {
        double dotProduct = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record IndexedDocument(RagDocument document, float[] embedding) {
    }
}
