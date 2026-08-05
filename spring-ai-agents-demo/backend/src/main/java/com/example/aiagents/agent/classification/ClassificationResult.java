package com.example.aiagents.agent.classification;

import com.example.aiagents.agent.Category;

import java.util.List;

public record ClassificationResult(Category category, double confidence, List<String> keywords) {
}
