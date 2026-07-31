package com.example.aishowcase.rag;

import com.example.aishowcase.config.OpenAiProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EmbeddingService {

    private final WebClient openAiWebClient;
    private final OpenAiProperties properties;

    public EmbeddingService(WebClient openAiWebClient, OpenAiProperties properties) {
        this.openAiWebClient = openAiWebClient;
        this.properties = properties;
    }

    public List<Double> embed(String text) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "OPENAI_API_KEY ist nicht gesetzt");
        }

        var request = new OpenAiEmbeddingRequest(properties.embeddingModel(), List.of(text));

        var response = openAiWebClient.post()
                .uri("/embeddings")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAiEmbeddingResponse.class)
                .block();

        return response.data().get(0).embedding();
    }
}
