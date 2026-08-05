package com.example.aiagents.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewTicketRequest(@NotBlank @Size(max = 8000) String text) {
}
