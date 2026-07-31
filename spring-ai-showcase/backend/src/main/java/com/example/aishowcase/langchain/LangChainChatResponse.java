package com.example.aishowcase.langchain;

public record LangChainChatResponse(String channel, String module, String sessionId, String message, String reply) {
}
