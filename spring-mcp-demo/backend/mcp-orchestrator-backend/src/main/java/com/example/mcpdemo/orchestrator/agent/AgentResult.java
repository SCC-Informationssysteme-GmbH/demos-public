package com.example.mcpdemo.orchestrator.agent;

import java.util.List;

public record AgentResult(String answer, List<AgentStep> steps) {
}
