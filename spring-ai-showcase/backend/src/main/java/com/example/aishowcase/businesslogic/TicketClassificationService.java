package com.example.aishowcase.businesslogic;

import com.example.aishowcase.config.OpenAiProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TicketClassificationService {

    private static final String SYSTEM_PROMPT = """
            Du bist ein Support-Ticket-Klassifizierer für ein Software-Unternehmen.
            Analysiere die Kundenanfrage und antworte ausschließlich mit einem JSON-Objekt exakt in diesem Format,
            ohne zusätzlichen Text:
            {"category": "BILLING|TECHNICAL|ACCOUNT|GENERAL", "priority": "LOW|MEDIUM|HIGH", \
            "summary": "Ein-Satz-Zusammenfassung auf Deutsch", \
            "suggestedReply": "Kurzer, freundlicher Antwortentwurf auf Deutsch"}
            """;

    private final WebClient openAiWebClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public TicketClassificationService(WebClient openAiWebClient, OpenAiProperties properties, ObjectMapper objectMapper) {
        this.openAiWebClient = openAiWebClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public TicketClassification classify(String ticketText) {
        var request = new StructuredChatRequest(
                properties.model(),
                List.of(
                        new StructuredChatRequest.Message("system", SYSTEM_PROMPT),
                        new StructuredChatRequest.Message("user", ticketText)
                ),
                new StructuredChatRequest.ResponseFormat("json_object")
        );

        var response = openAiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(StructuredChatResponse.class)
                .block();

        String json = response.choices().get(0).message().content();
        try {
            return objectMapper.readValue(json, TicketClassification.class);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Antwort des Modells konnte nicht als Klassifikation geparst werden", e);
        }
    }
}
