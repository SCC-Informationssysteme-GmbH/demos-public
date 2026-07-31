package de.scc.demo.tts.service;

import de.scc.demo.tts.config.TtsProperties;
import de.scc.demo.tts.dto.TtsRequest;
import de.scc.demo.tts.exception.TtsProviderException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TtsService {

    private final WebClient openAiWebClient;
    private final TtsProperties properties;

    public Flux<DataBuffer> synthesize(TtsRequest request) {
        boolean hasInstructions = request.instructions() != null && !request.instructions().isBlank();

        Map<String, Object> body = new HashMap<>();
        body.put("model", hasInstructions ? properties.instructionsModel() : properties.model());
        body.put("input", request.text());
        body.put("voice", request.voice() != null && !request.voice().isBlank() ? request.voice() : properties.defaultVoice());
        body.put("response_format", "mp3");
        if (request.speed() != null) {
            body.put("speed", request.speed());
        }
        if (hasInstructions) {
            body.put("instructions", request.instructions());
        }

        return openAiWebClient.post()
                .uri("/v1/audio/speech")
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::toProviderError)
                .bodyToFlux(DataBuffer.class);
    }

    private Mono<Throwable> toProviderError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(errorBody -> new TtsProviderException(response.statusCode(), errorBody));
    }
}
