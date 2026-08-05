package com.example.aiagents.agent.research;

import com.example.aiagents.agent.Agent;
import com.example.aiagents.knowledge.KnowledgeFilter;
import com.example.aiagents.knowledge.KnowledgeMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Recherche-Agent: Vektorsuche ueber die Wissensquellen, danach ein LLM-Call,
 * der nur aus den gefundenen Dokumenten zusammenfasst (kein freier Text ohne Belege).
 */
@Service
public class ResearchAgent implements Agent<ResearchInput, ResearchResult> {

    private static final Logger log = LoggerFactory.getLogger(ResearchAgent.class);

    private static final String SYSTEM_PROMPT = """
            Du bist ein Recherche-Agent. Fasse ausschliesslich Informationen aus den
            bereitgestellten Dokumenten zusammen, die zur Anfrage passen. Erfinde nichts.
            Gib in "sources" die verwendeten Dokumente mit Titel und einem kurzen Zitat an.
            Wenn die Dokumente nichts Passendes enthalten, sage das im "summary" deutlich
            und gib eine leere Quellenliste zurueck.
            """;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ResearchProperties properties;

    public ResearchAgent(ChatModel chatModel, VectorStore vectorStore,
                         ResearchProperties properties) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.vectorStore = vectorStore;
        this.properties = properties;
    }

    @Override
    public ResearchResult process(ResearchInput input) {
        List<Document> hits = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(input.text())
                        .topK(properties.topK())
                        .similarityThreshold(properties.similarityThreshold())
                        .filterExpression(
                                KnowledgeFilter.forCustomerFacingSearch(input.category()))
                        .build());

        logHits(input, hits);

        String documents = hits.stream()
                .map(this::render)
                .collect(Collectors.joining("\n\n---\n\n"));

        String userMessage = """
                Kategorie der Anfrage: %s

                Anfrage des Kunden:
                %s

                Gefundene Dokumente:
                %s
                """.formatted(input.category(), input.text(),
                documents.isBlank() ? "(keine Treffer)" : documents);

        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .call()
                .entity(ResearchResult.class);
    }

    /**
     * Ohne die Scores im Log ist der Schwellwert nicht einstellbar - man saehe nur, dass
     * Treffer fehlen, aber nicht, wie knapp sie daran vorbei waren.
     */
    private void logHits(ResearchInput input, List<Document> hits) {
        if (!log.isDebugEnabled()) {
            return;
        }
        log.debug("Recherche fuer {} (topK={}, threshold={}): {} Treffer",
                input.category(), properties.topK(), properties.similarityThreshold(),
                hits.size());
        for (Document hit : hits) {
            log.debug("   score={} source={}",
                    hit.getScore() == null ? "?" : String.format("%.4f", hit.getScore()),
                    hit.getMetadata().get(KnowledgeMetadata.SOURCE));
        }
    }

    private String render(Document document) {
        Object title = document.getMetadata().getOrDefault(KnowledgeMetadata.TITLE, "unbenannt");
        return "Titel: %s\n%s".formatted(title, document.getText());
    }
}
