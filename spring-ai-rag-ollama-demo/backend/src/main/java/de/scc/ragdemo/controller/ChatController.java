package de.scc.ragdemo.controller;

import de.scc.ragdemo.config.ChatClientConfig;
import de.scc.ragdemo.config.SystemPromptCatalog;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final String NO_CONTEXT_ANSWER =
            "Dazu finde ich nichts in den indexierten Dokumenten.";

    private final ChatClient chatClient;
    private final SystemPromptCatalog promptCatalog;
    private final VectorStore vectorStore;

    public ChatController(ChatClient chatClient, SystemPromptCatalog promptCatalog, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.promptCatalog = promptCatalog;
        this.vectorStore = vectorStore;
    }

    /**
     * Liste der auswaehlbaren Verhaltens-Aliase (siehe system-prompts.json),
     * fuer die Dropdown-Auswahl im Frontend.
     */
    @GetMapping("/personas")
    public List<SystemPromptCatalog.SystemPromptOption> personas() {
        return promptCatalog.options();
    }

    /**
     * Stellt eine Frage gegen die im Qdrant-Store indexierten Dokumente.
     * Der QuestionAnswerAdvisor (siehe ChatClientConfig) reichert den
     * Prompt automatisch mit den passendsten Chunks an. Optional kann per
     * {@code persona}-Alias ein anderer System-Prompt als der Standard
     * verwendet werden.
     *
     * <p>Fuer Personas mit {@code requiresContext=true} (aktuell "Nur Kontext
     * (streng)") verlassen wir uns dabei NICHT nur auf den System-Prompt -
     * kleine lokale Modelle halten sich daran erfahrungsgemaess nicht
     * zuverlaessig und beantworten Fragen trotzdem aus eigenem Wissen. Stattdessen
     * pruefen wir selbst per Aehnlichkeitssuche, ob ueberhaupt passender Kontext
     * existiert, und rufen das Modell in dem Fall gar nicht erst auf.
     */
    @PostMapping("/ask")
    public Map<String, String> ask(@RequestBody AskRequest request) {
        String persona = request.persona();

        if (promptCatalog.requiresContext(persona) && !hasMatchingContext(request.question())) {
            return Map.of("question", request.question(), "answer", NO_CONTEXT_ANSWER);
        }

        ChatClient.ChatClientRequestSpec spec = chatClient.prompt();
        if (StringUtils.hasText(persona)) {
            spec = spec.system(promptCatalog.promptForAlias(persona));
        }

        String answer = spec.user(request.question())
                .call()
                .content();
        return Map.of("question", request.question(),
                "answer", answer != null ? answer : "Keine Antwort erhalten.");
    }

    private boolean hasMatchingContext(String question) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(ChatClientConfig.RETRIEVAL_TOP_K)
                .similarityThreshold(ChatClientConfig.RETRIEVAL_SIMILARITY_THRESHOLD)
                .build();
        return !vectorStore.similaritySearch(searchRequest).isEmpty();
    }

    public record AskRequest(String question, String persona) {}
}
