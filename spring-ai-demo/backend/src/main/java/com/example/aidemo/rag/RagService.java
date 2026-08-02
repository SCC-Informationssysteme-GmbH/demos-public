package com.example.aidemo.rag;

import com.example.aidemo.llmrest.OpenAiChatService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private static final int TOP_K = 2;
    private static final String SYSTEM_PROMPT =
            "Du beantwortest Fragen ausschließlich anhand des gegebenen Kontexts. "
                    + "Wenn die Antwort nicht im Kontext enthalten ist, sage das offen, statt zu spekulieren.";

    private final RagVectorIndex vectorIndex;
    private final OpenAiChatService chatService;

    public RagService(RagVectorIndex vectorIndex, OpenAiChatService chatService) {
        this.vectorIndex = vectorIndex;
        this.chatService = chatService;
    }

    public RagAnswer ask(String question) {
        List<RagDocument> sources = vectorIndex.search(question, TOP_K);

        String context = sources.stream()
                .map(document -> "- " + document.title() + ": " + document.content())
                .collect(Collectors.joining("\n"));

        String userPrompt = "Kontext:\n" + context + "\n\nFrage: " + question;
        String answer = chatService.complete(SYSTEM_PROMPT, userPrompt);

        return new RagAnswer(sources, answer);
    }
}
