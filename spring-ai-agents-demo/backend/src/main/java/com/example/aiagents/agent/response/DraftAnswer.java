package com.example.aiagents.agent.response;

import com.example.aiagents.agent.SourceRef;

import java.util.List;

public record DraftAnswer(String text, List<SourceRef> sources, double confidence) {
}
