package com.example.aidemo.businesslogic;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class TicketClassificationService {

    private static final String SYSTEM_PROMPT = """
            Du bist ein Support-Ticket-Klassifizierer für ein Software-Unternehmen.
            Analysiere die Kundenanfrage und ordne sie einer Kategorie und Prioritaet zu,
            fasse sie in einem Satz auf Deutsch zusammen und formuliere einen kurzen,
            freundlichen Antwortentwurf auf Deutsch.
            """;

    private final ChatClient chatClient;

    public TicketClassificationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public TicketClassification classify(String ticketText) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(ticketText)
                .call()
                .entity(TicketClassification.class);
    }
}
