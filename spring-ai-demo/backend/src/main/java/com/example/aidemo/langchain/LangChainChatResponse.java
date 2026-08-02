package com.example.aidemo.langchain;

public record LangChainChatResponse(String channel, String module, String sessionId, String message, String reply) {
}
