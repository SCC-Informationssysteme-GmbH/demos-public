package com.example.mcpdemo.orchestrator.web;

import com.example.mcpdemo.orchestrator.OrchestratorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final OrchestratorService orchestratorService;

    public ChatController(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    public record ChatRequest(String message) {
    }

    public record ChatResponse(String reply) {
    }

    @PostMapping("/api/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return new ChatResponse(orchestratorService.chat(request.message()));
    }
}
