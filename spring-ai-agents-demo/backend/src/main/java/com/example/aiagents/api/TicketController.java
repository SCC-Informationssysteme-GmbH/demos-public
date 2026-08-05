package com.example.aiagents.api;

import com.example.aiagents.agent.response.DraftAnswer;
import com.example.aiagents.domain.Ticket;
import com.example.aiagents.domain.TicketRepository;
import com.example.aiagents.orchestration.TicketOrchestrator;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

/**
 * Reine Transportschicht: HTTP annehmen/validieren, an den Orchestrator weiterreichen.
 * Keine Geschaeftslogik.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;
    private final TicketOrchestrator orchestrator;
    private final TicketEventPublisher eventPublisher;

    public TicketController(TicketRepository ticketRepository,
                            TicketOrchestrator orchestrator,
                            TicketEventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.orchestrator = orchestrator;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    public ResponseEntity<TicketView> create(@Valid @RequestBody NewTicketRequest request) {
        Ticket ticket = ticketRepository.save(Ticket.newTicket(request.text()));
        orchestrator.runWorkflow(ticket.getId());
        return ResponseEntity.accepted().body(TicketView.of(ticket));
    }

    @GetMapping
    public List<TicketView> list() {
        return ticketRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(TicketView::of)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketView> getStatus(@PathVariable UUID id) {
        return ticketRepository.findById(id)
                .map(TicketView::of)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/draft")
    public ResponseEntity<DraftAnswer> getDraft(@PathVariable UUID id) {
        return ticketRepository.findById(id)
                .map(Ticket::getDraft)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<TicketView> approve(@PathVariable UUID id,
                                              @RequestBody ApprovalDecision decision) {
        return ResponseEntity.ok(TicketView.of(orchestrator.finalizeTicket(id, decision)));
    }

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable UUID id) {
        return eventPublisher.subscribe(id);
    }
}
