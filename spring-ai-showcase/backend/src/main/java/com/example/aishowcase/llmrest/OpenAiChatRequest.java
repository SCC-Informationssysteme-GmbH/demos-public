package com.example.aishowcase.llmrest;

import java.util.List;

record OpenAiChatRequest(String model, List<Message> messages) {
    record Message(String role, String content) {
    }
}
