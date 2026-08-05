package com.example.aiagents.agent.response;

import com.example.aiagents.agent.classification.ClassificationResult;
import com.example.aiagents.agent.research.ResearchResult;

public record ResponseInput(String originalText,
                            ClassificationResult classification,
                            ResearchResult research) {
}
