package de.scc.demo.tts.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai.tts")
public record TtsProperties(String apiKey, String baseUrl, String model, String defaultVoice, String instructionsModel) {
}
