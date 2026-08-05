package com.example.aiagents.agent.response;

import com.example.aiagents.agent.Agent;
import com.example.aiagents.agent.SourceRef;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Kombiniert Klassifizierung und Rechercheergebnis zu einem Antwortentwurf.
 * Der Entwurf geht NICHT direkt an den Kunden, sondern in die Mitarbeiter-Freigabe.
 */
@Service
public class ResponseAgent implements Agent<ResponseInput, DraftAnswer> {

    private static final String SYSTEM_PROMPT = """
            Du bist ein Antwort-Agent. Formuliere auf Basis von Klassifizierung und
            Rechercheergebnis eine hoefliche, sachlich korrekte Antwort an den Kunden
            auf Deutsch. Kennzeichne unsichere Aussagen explizit. Erfinde keine Fakten,
            die nicht im Rechercheergebnis stehen.
            Uebernimm in "sources" die Quellen, auf die du dich stuetzt.
            "confidence" ist ein Wert zwischen 0 und 1 fuer deine Sicherheit.
            """;

    private final ChatClient chatClient;

    public ResponseAgent(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public DraftAnswer process(ResponseInput input) {
        String userMessage = """
                Anfrage des Kunden:
                %s

                Klassifizierung: %s (confidence %.2f)

                Rechercheergebnis:
                %s

                Quellen:
                %s
                """.formatted(
                input.originalText(),
                input.classification().category(),
                input.classification().confidence(),
                input.research() == null ? "(keine Recherche durchgefuehrt)" : input.research().summary(),
                renderSources(input.research() == null ? List.of() : input.research().sources()));

        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .call()
                .entity(DraftAnswer.class);
    }

    private String renderSources(List<SourceRef> sources) {
        if (sources == null || sources.isEmpty()) {
            return "(keine)";
        }
        return sources.stream()
                .map(s -> "- %s: %s".formatted(s.title(), s.snippet()))
                .collect(Collectors.joining("\n"));
    }
}
