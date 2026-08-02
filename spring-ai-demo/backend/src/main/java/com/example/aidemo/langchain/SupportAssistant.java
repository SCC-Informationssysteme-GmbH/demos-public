package com.example.aidemo.langchain;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface SupportAssistant {

    @SystemMessage("Du bist ein freundlicher Kundensupport-Assistent für ein Software-Unternehmen. "
            + "Antworte kurz und konkret und beziehe dich, wenn sinnvoll, auf den bisherigen Gesprächsverlauf.")
    String chat(@MemoryId String sessionId, @UserMessage String message);
}
