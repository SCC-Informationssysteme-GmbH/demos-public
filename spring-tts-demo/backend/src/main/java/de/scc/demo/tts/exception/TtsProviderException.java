package de.scc.demo.tts.exception;

import org.springframework.http.HttpStatusCode;

public class TtsProviderException extends RuntimeException {

    private final HttpStatusCode providerStatus;

    public TtsProviderException(HttpStatusCode providerStatus, String providerBody) {
        super("OpenAI TTS request failed with status " + providerStatus.value() + ": " + providerBody);
        this.providerStatus = providerStatus;
    }

    public HttpStatusCode providerStatus() {
        return providerStatus;
    }
}
