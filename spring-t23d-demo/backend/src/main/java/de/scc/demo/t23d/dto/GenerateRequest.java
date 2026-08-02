package de.scc.demo.t23d.dto;

import jakarta.validation.constraints.NotBlank;

public record GenerateRequest(@NotBlank String prompt) {
}
