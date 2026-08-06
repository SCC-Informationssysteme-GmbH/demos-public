package de.scc.ragdemo.controller;

import io.qdrant.client.QdrantClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Minimaler Ingestion-Pfad fuer die Demo: Text wird per TokenTextSplitter
 * in Chunks zerlegt, ueber das konfigurierte Ollama-Embedding-Modell
 * vektorisiert und in Qdrant abgelegt (VectorStore.add uebernimmt beides).
 *
 * Fuer PDFs/Office-Formate kann zusaetzlich spring-ai-pdf-document-reader
 * bzw. ein Tika-Reader eingebunden werden - fuer die Demo reicht Klartext
 * (txt/md) aus.
 */
@RestController
@RequestMapping("/api/documents")
public class IngestController {

    private final VectorStore vectorStore;
    private final QdrantClient qdrantClient;
    private final QdrantVectorStoreProperties qdrantProperties;
    private final TokenTextSplitter splitter = TokenTextSplitter.builder().build();

    public IngestController(VectorStore vectorStore, QdrantClient qdrantClient,
            QdrantVectorStoreProperties qdrantProperties) {
        this.vectorStore = vectorStore;
        this.qdrantClient = qdrantClient;
        this.qdrantProperties = qdrantProperties;
    }

    /**
     * Anzahl der aktuell in Qdrant indexierten Chunks (Punkte in der Collection),
     * damit das Frontend anzeigen kann, wie "gefuellt" die Wissensbasis ist.
     */
    @GetMapping("/count")
    public Map<String, Object> count() {
        String collection = qdrantProperties.getCollectionName();
        long count;
        try {
            count = qdrantClient.countAsync(collection).get();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Qdrant-Anfrage unterbrochen", e);
        }
        catch (ExecutionException e) {
            throw new IllegalStateException("Qdrant-Zaehlung fehlgeschlagen", e);
        }
        return Map.of("collection", collection, "count", count);
    }

    @PostMapping
    public Map<String, Object> ingestText(@RequestBody IngestRequest request) {
        Document document = new Document(
                request.content(),
                Map.of("source", request.source() != null ? request.source() : "inline"));

        List<Document> chunks = splitter.apply(List.of(document));
        vectorStore.add(chunks);

        return Map.of("chunksIndexed", chunks.size());
    }

    @PostMapping("/upload")
    public Map<String, Object> ingestFile(@RequestParam("file") MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String source = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload";

        Document document = new Document(content, Map.of("source", source));
        List<Document> chunks = splitter.apply(List.of(document));
        vectorStore.add(chunks);

        return Map.of("chunksIndexed", chunks.size(), "source", source);
    }

    public record IngestRequest(String content, String source) {}
}
