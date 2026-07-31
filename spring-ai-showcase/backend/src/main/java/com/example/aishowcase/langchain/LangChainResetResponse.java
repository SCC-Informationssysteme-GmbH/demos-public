package com.example.aishowcase.langchain;

public record LangChainResetResponse(String channel, String module, String sessionId, boolean reset) {
}
