package com.example.aidemo.prompt;

import java.util.Map;

public record PromptTemplate(String key, String label, String systemPrompt, String userTemplate) {

    public String render(String input) {
        return new org.springframework.ai.chat.prompt.PromptTemplate(userTemplate).render(Map.of("input", input));
    }
}
