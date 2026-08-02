package com.example.aidemo.llmrest;

public record ChatResponse(String channel, String module, String prompt, String reply) {
}
