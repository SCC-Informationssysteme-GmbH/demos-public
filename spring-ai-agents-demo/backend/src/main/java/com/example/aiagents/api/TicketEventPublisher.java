package com.example.aiagents.api;

import com.example.aiagents.domain.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Haelt die offenen SSE-Verbindungen pro Ticket und schiebt Statuswechsel raus.
 * Bewusst In-Memory: fuer mehrere Instanzen muesste das ueber einen Broker laufen.
 */
@Component
public class TicketEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TicketEventPublisher.class);
    private static final long TIMEOUT_MS = 10 * 60 * 1000L;

    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID ticketId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.computeIfAbsent(ticketId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(ticketId, emitter));
        emitter.onTimeout(() -> remove(ticketId, emitter));
        emitter.onError(e -> remove(ticketId, emitter));
        return emitter;
    }

    public void publish(UUID ticketId, TicketState state) {
        List<SseEmitter> targets = emitters.get(ticketId);
        if (targets == null || targets.isEmpty()) {
            return;
        }
        TicketStatusEvent event = new TicketStatusEvent(ticketId, state, Instant.now());
        for (SseEmitter emitter : targets) {
            try {
                emitter.send(SseEmitter.event().name("status").data(event));
            } catch (IOException | IllegalStateException e) {
                log.debug("SSE-Verbindung fuer Ticket {} geschlossen", ticketId);
                remove(ticketId, emitter);
            }
        }
    }

    private void remove(UUID ticketId, SseEmitter emitter) {
        List<SseEmitter> targets = emitters.get(ticketId);
        if (targets != null) {
            targets.remove(emitter);
            if (targets.isEmpty()) {
                emitters.remove(ticketId);
            }
        }
    }
}
