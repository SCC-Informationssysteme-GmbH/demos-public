package com.example.aishowcase.llmrest;

import com.example.aishowcase.config.OpenAiProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OpenAiChatService {

    private final WebClient openAiWebClient;
    private final OpenAiProperties properties;

    public OpenAiChatService(WebClient openAiWebClient, OpenAiProperties properties) {
        this.openAiWebClient = openAiWebClient;
        this.properties = properties;
    }

    public String complete(String userPrompt) {
        return complete("Du bist ein hilfreicher Assistent.", userPrompt);
    }

    public String complete(String systemPrompt, String userPrompt) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "OPENAI_API_KEY ist nicht gesetzt");
        }

        var request = new OpenAiChatRequest(
                properties.model(),
                List.of(
                        new OpenAiChatRequest.Message("system", systemPrompt),
                        new OpenAiChatRequest.Message("user", userPrompt)
                )
        );

        var response = openAiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAiChatResponse.class)
                .block();

        return response.choices().get(0).message().content();
    }
}
