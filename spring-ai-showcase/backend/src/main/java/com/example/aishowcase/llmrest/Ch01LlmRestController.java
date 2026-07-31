package com.example.aishowcase.llmrest;

import com.example.aishowcase.common.ChannelStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ch01")
public class Ch01LlmRestController {

    private final OpenAiChatService chatService;

    public Ch01LlmRestController(OpenAiChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/status")
    public ChannelStatusResponse status() {
        return new ChannelStatusResponse("CH.01", "llmrest", "LLM per REST bereit");
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String reply = chatService.complete(request.prompt());
        return new ChatResponse("CH.01", "llmrest", request.prompt(), reply);
    }
}
