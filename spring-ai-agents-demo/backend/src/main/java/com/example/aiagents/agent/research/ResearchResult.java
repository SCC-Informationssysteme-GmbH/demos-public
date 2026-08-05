package com.example.aiagents.agent.research;

import com.example.aiagents.agent.SourceRef;

import java.util.List;

public record ResearchResult(String summary, List<SourceRef> sources) {
}
