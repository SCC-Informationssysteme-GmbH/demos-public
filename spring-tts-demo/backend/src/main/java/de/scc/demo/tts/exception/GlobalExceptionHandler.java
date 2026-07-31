package de.scc.demo.tts.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TtsProviderException.class)
    public Mono<ResponseEntity<String>> handleTtsProviderException(TtsProviderException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ex.getMessage()));
    }
}
