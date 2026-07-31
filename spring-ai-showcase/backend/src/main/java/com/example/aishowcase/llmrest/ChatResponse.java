package com.example.aishowcase.llmrest;

public record ChatResponse(String channel, String module, String prompt, String reply) {
}
