package com.example.aishowcase.llmrest;

import java.util.List;

record OpenAiChatResponse(List<Choice> choices) {
    record Choice(Message message) {
    }

    record Message(String role, String content) {
    }
}
