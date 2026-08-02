package de.scc.demo.freecad.dto;

import jakarta.validation.constraints.NotBlank;

public record GenerateRequest(@NotBlank String prompt) {
}
