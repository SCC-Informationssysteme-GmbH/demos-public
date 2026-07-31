package de.scc.demo.tts.controller;

import de.scc.demo.tts.dto.TtsRequest;
import de.scc.demo.tts.service.TtsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
public class TtsController {

    private final TtsService ttsService;

    @Operation(summary = "Text per OpenAI TTS in Sprache umwandeln")
    @ApiResponse(responseCode = "200", description = "Audio (MP3)",
            content = @Content(mediaType = "audio/mpeg", schema = @Schema(type = "string", format = "binary")))
    @PostMapping(produces = "audio/mpeg")
    public ResponseEntity<Flux<DataBuffer>> synthesize(@Valid @RequestBody TtsRequest request) {
        Flux<DataBuffer> audio = ttsService.synthesize(request);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(audio);
    }
}
