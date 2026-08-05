package com.example.aiagents.api;

import com.example.aiagents.domain.TicketState;

import java.time.Instant;
import java.util.UUID;

public record TicketStatusEvent(UUID ticketId, TicketState state, Instant at) {
}
