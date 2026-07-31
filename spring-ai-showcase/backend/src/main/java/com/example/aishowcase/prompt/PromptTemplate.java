package com.example.aishowcase.prompt;

public record PromptTemplate(String key, String label, String systemPrompt, String userTemplate) {

    public String render(String input) {
        return userTemplate.replace("{{input}}", input);
    }
}
