package com.example.mcpdemo.orchestrator.web;

import com.example.mcpdemo.orchestrator.OrchestratorService;
import com.example.mcpdemo.orchestrator.agent.AgentResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgentController {

    private final OrchestratorService orchestratorService;

    public AgentController(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    public record AgentRequest(String task) {
    }

    @PostMapping("/api/agent")
    public AgentResult runAgent(@RequestBody AgentRequest request) {
        return orchestratorService.runAgent(request.task());
    }
}
