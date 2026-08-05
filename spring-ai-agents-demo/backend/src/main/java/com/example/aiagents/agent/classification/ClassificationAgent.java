package com.example.aiagents.agent.classification;

import com.example.aiagents.agent.Agent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

/**
 * Reiner Text-in/JSON-out-Agent, kein Tool- und kein DB-Zugriff.
 */
@Service
public class ClassificationAgent implements Agent<TicketInput, ClassificationResult> {

    private static final String SYSTEM_PROMPT = """
            Du bist ein Klassifizierungs-Agent fuer Kundenanfragen.
            Ordne den Text in GENAU eine Kategorie ein:
            TECHNISCHES_PROBLEM, VERTRAGSFRAGE, FEATURE_WUNSCH, SONSTIGES.
            confidence ist ein Wert zwischen 0 und 1.
            keywords sind maximal fuenf praegnante Begriffe aus dem Text.
            """;

    private final ChatClient chatClient;

    public ClassificationAgent(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public ClassificationResult process(TicketInput input) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(input.text())
                .call()
                .entity(ClassificationResult.class);
    }
}
