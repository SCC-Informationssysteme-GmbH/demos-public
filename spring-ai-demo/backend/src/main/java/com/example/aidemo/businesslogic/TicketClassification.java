package com.example.aidemo.businesslogic;

public record TicketClassification(TicketCategory category, TicketPriority priority, String summary, String suggestedReply) {
}
