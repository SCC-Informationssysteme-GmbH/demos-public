package com.example.aidemo.langchain;

public record LangChainResetResponse(String channel, String module, String sessionId, boolean reset) {
}
