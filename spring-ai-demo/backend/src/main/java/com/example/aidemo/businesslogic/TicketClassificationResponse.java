package com.example.aidemo.businesslogic;

public record TicketClassificationResponse(String channel, String module, String ticketText, TicketClassification classification) {
}
