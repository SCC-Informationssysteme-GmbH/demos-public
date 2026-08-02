package com.example.aidemo.langchain;

import com.example.aidemo.config.OpenAiProperties;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChainConfig {

    @Bean
    public ChatModel chatModel(OpenAiProperties properties) {
        return OpenAiChatModel.builder()
                .apiKey(properties.apiKey())
                .modelName(properties.model())
                .build();
    }

    @Bean
    public SupportAssistant supportAssistant(ChatModel chatModel, SessionChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(SupportAssistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }
}
