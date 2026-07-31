package de.scc.demo.tts.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TtsRequest(
        @NotBlank @Size(max = 4096) String text,
        String voice,
        @DecimalMin("0.25") @DecimalMax("4.0") Double speed,
        @Size(max = 1024) String instructions
) {
}
