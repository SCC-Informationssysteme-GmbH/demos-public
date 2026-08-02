package com.example.aidemo.langchain;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hält die {@link ChatMemory}-Instanzen selbst statt sie LangChain4j intern verwalten zu lassen
 * ({@code AiServices.chatMemoryProvider(sessionId -> ...)} gibt sonst keine Referenz zurück,
 * über die sich eine einzelne Session zurücksetzen ließe).
 */
@Component
public class SessionChatMemoryProvider implements ChatMemoryProvider {

    private static final int MAX_MESSAGES = 10;

    private final Map<Object, ChatMemory> memories = new ConcurrentHashMap<>();

    @Override
    public ChatMemory get(Object memoryId) {
        return memories.computeIfAbsent(memoryId, id -> MessageWindowChatMemory.withMaxMessages(MAX_MESSAGES));
    }

    public boolean reset(String sessionId) {
        ChatMemory memory = memories.get(sessionId);
        if (memory == null) {
            return false;
        }
        memory.clear();
        return true;
    }
}
