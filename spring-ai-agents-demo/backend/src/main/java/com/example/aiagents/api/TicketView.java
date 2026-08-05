package com.example.aiagents.api;

import com.example.aiagents.agent.classification.ClassificationResult;
import com.example.aiagents.agent.research.ResearchResult;
import com.example.aiagents.agent.response.DraftAnswer;
import com.example.aiagents.domain.Ticket;
import com.example.aiagents.domain.TicketState;

import java.time.Instant;
import java.util.UUID;

public record TicketView(UUID id,
                         String customerText,
                         TicketState state,
                         ClassificationResult classification,
                         ResearchResult research,
                         DraftAnswer draft,
                         String finalText,
                         Instant createdAt,
                         Instant updatedAt) {

    public static TicketView of(Ticket ticket) {
        return new TicketView(
                ticket.getId(),
                ticket.getCustomerText(),
                ticket.getState(),
                ticket.getClassification(),
                ticket.getResearch(),
                ticket.getDraft(),
                ticket.getFinalText(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }
}
