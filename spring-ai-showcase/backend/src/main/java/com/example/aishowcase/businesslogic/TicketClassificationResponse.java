package com.example.aishowcase.businesslogic;

public record TicketClassificationResponse(String channel, String module, String ticketText, TicketClassification classification) {
}
