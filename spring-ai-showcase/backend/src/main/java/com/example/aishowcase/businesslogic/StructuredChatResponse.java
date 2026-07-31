package com.example.aishowcase.businesslogic;

import java.util.List;

record StructuredChatResponse(List<Choice> choices) {
    record Choice(Message message) {
    }

    record Message(String role, String content) {
    }
}
