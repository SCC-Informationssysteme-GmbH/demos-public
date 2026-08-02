package com.example.aidemo.llmrest;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class OpenAiChatService {

    private final ChatClient chatClient;

    public OpenAiChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String complete(String userPrompt) {
        return complete("Du bist ein hilfreicher Assistent.", userPrompt);
    }

    public String complete(String systemPrompt, String userPrompt) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }
}
