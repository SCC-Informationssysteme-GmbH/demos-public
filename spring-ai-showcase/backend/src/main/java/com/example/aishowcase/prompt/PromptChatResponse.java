package com.example.aishowcase.prompt;

public record PromptChatResponse(String channel, String module, String templateKey, String renderedPrompt, String reply) {
}
