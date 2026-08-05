package com.example.aiagents.domain;

import com.example.aiagents.agent.classification.ClassificationResult;
import com.example.aiagents.agent.research.ResearchResult;
import com.example.aiagents.agent.response.DraftAnswer;
import jakarta.persistence.Converter;

public final class Converters {

    private Converters() {
    }

    @Converter
    public static class ClassificationResultConverter extends JsonAttributeConverter<ClassificationResult> {
        public ClassificationResultConverter() {
            super(ClassificationResult.class);
        }
    }

    @Converter
    public static class ResearchResultConverter extends JsonAttributeConverter<ResearchResult> {
        public ResearchResultConverter() {
            super(ResearchResult.class);
        }
    }

    @Converter
    public static class DraftAnswerConverter extends JsonAttributeConverter<DraftAnswer> {
        public DraftAnswerConverter() {
            super(DraftAnswer.class);
        }
    }
}
