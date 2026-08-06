package de.scc.ragdemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueft das tatsaechliche Verhalten der vier auswaehlbaren Personas
 * (system-prompts.json) gegen das echte, lokal laufende Ollama-Modell -
 * bewusst kein Mock, weil genau die reale Modell-Antwort das Interessante ist.
 *
 * Frage ist absichtlich allgemeines Wissen, das NICHT in den indexierten
 * Test-Dokumenten steht: so zeigt sich, ob "Nur Kontext (streng)" tatsaechlich
 * auf den fehlenden Kontext verweist, oder trotzdem aus dem Modellwissen antwortet.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatControllerPersonaTest {

    private static final String QUESTION = "Was ist die Hauptstadt von Frankreich?";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @LocalServerPort
    private int port;

    @ParameterizedTest
    @ValueSource(strings = {"rag-fallback", "general", "concise"})
    void nichtStriktePersonasAntwortenAusAllgemeinwissen(String persona) throws Exception {
        String answer = ask(persona);
        assertThat(answer.toLowerCase()).contains("paris");
    }

    @Test
    void striktePersonaVerweigertOhnePassendenKontext() throws Exception {
        String answer = ask("rag-strict");
        assertThat(answer.toLowerCase()).doesNotContain("paris");
    }

    private String ask(String persona) throws Exception {
        String requestBody = MAPPER.writeValueAsString(Map.of("question", QUESTION, "persona", persona));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/chat/ask"))
                .timeout(Duration.ofSeconds(90))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        Map<?, ?> body = MAPPER.readValue(response.body(), Map.class);
        return (String) body.get("answer");
    }
}
