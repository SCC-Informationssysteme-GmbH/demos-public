package com.example.aishowcase.businesslogic;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

record StructuredChatRequest(String model, List<Message> messages, @JsonProperty("response_format") ResponseFormat responseFormat) {
    record Message(String role, String content) {
    }

    record ResponseFormat(String type) {
    }
}
