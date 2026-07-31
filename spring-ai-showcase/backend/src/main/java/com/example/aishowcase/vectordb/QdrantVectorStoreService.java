package com.example.aishowcase.vectordb;

import com.example.aishowcase.config.QdrantProperties;
import com.example.aishowcase.rag.EmbeddingService;
import com.example.aishowcase.rag.RagDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class QdrantVectorStoreService {

    private static final int EMBEDDING_DIMENSION = 1536;

    private final WebClient qdrantWebClient;
    private final EmbeddingService embeddingService;
    private final QdrantProperties properties;

    public QdrantVectorStoreService(WebClient qdrantWebClient, EmbeddingService embeddingService, QdrantProperties properties) {
        this.qdrantWebClient = qdrantWebClient;
        this.embeddingService = embeddingService;
        this.properties = properties;
    }

    public int indexDocuments(List<RagDocument> documents) {
        ensureCollection();

        List<QdrantPoint> points = documents.stream()
                .map(document -> new QdrantPoint(
                        toPointId(document.id()),
                        embeddingService.embed(document.content()),
                        Map.of("docId", document.id(), "title", document.title(), "content", document.content())))
                .toList();

        qdrantWebClient.put()
                .uri("/collections/{collection}/points", properties.collection())
                .bodyValue(new QdrantUpsertRequest(points))
                .retrieve()
                .toBodilessEntity()
                .block();

        return points.size();
    }

    public List<VectorMatch> search(String query, int topK) {
        List<Double> queryVector = embeddingService.embed(query);

        QdrantSearchResponse response = qdrantWebClient.post()
                .uri("/collections/{collection}/points/search", properties.collection())
                .bodyValue(new QdrantSearchRequest(queryVector, topK, true))
                .retrieve()
                .bodyToMono(QdrantSearchResponse.class)
                .block();

        return response.result().stream()
                .map(result -> new VectorMatch(
                        (String) result.payload().get("docId"),
                        (String) result.payload().get("title"),
                        result.score()))
                .toList();
    }

    private void ensureCollection() {
        try {
            qdrantWebClient.get()
                    .uri("/collections/{collection}", properties.collection())
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException.NotFound notFound) {
            qdrantWebClient.put()
                    .uri("/collections/{collection}", properties.collection())
                    .bodyValue(new QdrantCreateCollectionRequest(
                            new QdrantCreateCollectionRequest.VectorsConfig(EMBEDDING_DIMENSION, "Cosine")))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        }
    }

    private String toPointId(String docId) {
        return UUID.nameUUIDFromBytes(docId.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
