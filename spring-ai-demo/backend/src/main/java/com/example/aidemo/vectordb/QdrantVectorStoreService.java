package com.example.aidemo.vectordb;

import com.example.aidemo.rag.RagDocument;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class QdrantVectorStoreService {

    private final VectorStore vectorStore;

    public QdrantVectorStoreService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public int indexDocuments(List<RagDocument> documents) {
        // Qdrant-Point-IDs muessen UUIDs sein; die fachliche docId bleibt zum Wiederauffinden im Metadata-Payload erhalten.
        List<Document> points = documents.stream()
                .map(document -> new Document(
                        toPointId(document.id()),
                        document.content(),
                        Map.of("docId", document.id(), "title", document.title())))
                .toList();

        vectorStore.add(points);

        return points.size();
    }

    public List<VectorMatch> search(String query, int topK) {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(topK).build());

        return results.stream()
                .map(document -> new VectorMatch(
                        (String) document.getMetadata().get("docId"),
                        (String) document.getMetadata().get("title"),
                        document.getScore() != null ? document.getScore() : 0.0))
                .toList();
    }

    private String toPointId(String docId) {
        return UUID.nameUUIDFromBytes(docId.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
