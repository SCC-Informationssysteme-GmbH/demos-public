package com.example.aishowcase.langchain;

import com.example.aishowcase.common.ChannelStatusResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ch05")
public class Ch05LangChainController {

    private final SupportAssistant supportAssistant;
    private final SessionChatMemoryProvider chatMemoryProvider;

    public Ch05LangChainController(SupportAssistant supportAssistant, SessionChatMemoryProvider chatMemoryProvider) {
        this.supportAssistant = supportAssistant;
        this.chatMemoryProvider = chatMemoryProvider;
    }

    @GetMapping("/status")
    public ChannelStatusResponse status() {
        return new ChannelStatusResponse("CH.05", "langchain", "LangChain4J-Service bereit");
    }

    @PostMapping("/chat")
    public LangChainChatResponse chat(@RequestBody LangChainChatRequest request) {
        String sessionId = (request.sessionId() == null || request.sessionId().isBlank())
                ? "default"
                : request.sessionId();
        String reply = supportAssistant.chat(sessionId, request.message());
        return new LangChainChatResponse("CH.05", "langchain", sessionId, request.message(), reply);
    }

    @DeleteMapping("/chat/{sessionId}")
    public LangChainResetResponse reset(@PathVariable String sessionId) {
        boolean wasReset = chatMemoryProvider.reset(sessionId);
        return new LangChainResetResponse("CH.05", "langchain", sessionId, wasReset);
    }
}
