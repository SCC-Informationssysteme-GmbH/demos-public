package com.example.aiagents.orchestration;

import com.example.aiagents.agent.classification.ClassificationAgent;
import com.example.aiagents.agent.classification.ClassificationResult;
import com.example.aiagents.agent.classification.TicketInput;
import com.example.aiagents.agent.research.ResearchAgent;
import com.example.aiagents.agent.research.ResearchInput;
import com.example.aiagents.agent.research.ResearchResult;
import com.example.aiagents.agent.response.DraftAnswer;
import com.example.aiagents.agent.response.ResponseAgent;
import com.example.aiagents.agent.response.ResponseInput;
import com.example.aiagents.api.ApprovalDecision;
import com.example.aiagents.api.TicketEventPublisher;
import com.example.aiagents.domain.Ticket;
import com.example.aiagents.domain.TicketRepository;
import com.example.aiagents.domain.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Herzstueck, aber selbst kein Agent: reine Ablaufsteuerung.
 * Kennt die Reihenfolge und persistiert jedes Zwischenergebnis; die Agenten
 * kennen weder den Ablauf noch einander noch die Datenbank.
 */
@Service
public class TicketOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(TicketOrchestrator.class);

    private final ClassificationAgent classificationAgent;
    private final ResearchAgent researchAgent;
    private final ResponseAgent responseAgent;
    private final WorkflowPlanRegistry workflowPlanRegistry;
    private final TicketRepository ticketRepository;
    private final TicketEventPublisher eventPublisher;
    private final CustomerNotificationService notificationService;

    public TicketOrchestrator(ClassificationAgent classificationAgent,
                              ResearchAgent researchAgent,
                              ResponseAgent responseAgent,
                              WorkflowPlanRegistry workflowPlanRegistry,
                              TicketRepository ticketRepository,
                              TicketEventPublisher eventPublisher,
                              CustomerNotificationService notificationService) {
        this.classificationAgent = classificationAgent;
        this.researchAgent = researchAgent;
        this.responseAgent = responseAgent;
        this.workflowPlanRegistry = workflowPlanRegistry;
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
        this.notificationService = notificationService;
    }

    @Async
    public void runWorkflow(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        try {
            ClassificationResult classification =
                    classificationAgent.process(new TicketInput(ticket.getCustomerText()));
            ticket.setClassification(classification);
            advance(ticket, TicketState.CLASSIFIED);

            WorkflowPlan plan = workflowPlanRegistry.planFor(classification.category());

            if (plan.runResearch()) {
                ResearchResult research = researchAgent.process(
                        new ResearchInput(ticket.getCustomerText(), classification.category()));
                ticket.setResearch(research);
                advance(ticket, TicketState.RESEARCHED);
            }

            if (plan.runResponseDraft()) {
                DraftAnswer draft = responseAgent.process(new ResponseInput(
                        ticket.getCustomerText(), classification, ticket.getResearch()));
                ticket.setDraft(draft);
                advance(ticket, TicketState.AWAITING_APPROVAL);
            } else {
                // Kein Auto-Entwurf. Manche Kategorien schulden dem Kunden trotzdem eine
                // Rueckmeldung - die kommt aus einer festen Vorlage und geht ohne Freigabe.
                if (plan.sendAutoAcknowledgement()) {
                    ticket.setFinalText(notificationService.acknowledgement(
                            ticket, classification.category()));
                }
                advance(ticket, plan.terminalStateIfSkipped());
            }
        } catch (RuntimeException e) {
            log.error("Workflow fuer Ticket {} fehlgeschlagen", ticketId, e);
            advance(ticket, TicketState.REJECTED);
        }
    }

    public Ticket finalizeTicket(UUID ticketId, ApprovalDecision decision) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        if (ticket.getState() != TicketState.AWAITING_APPROVAL) {
            throw new IllegalStateException(
                    "Ticket %s ist nicht im Zustand AWAITING_APPROVAL (aktuell: %s)"
                            .formatted(ticketId, ticket.getState()));
        }

        if (decision.approved()) {
            String text = (decision.editedText() == null || decision.editedText().isBlank())
                    ? ticket.getDraft().text()
                    : decision.editedText();
            ticket.setFinalText(text);
            // Hier wuerde der Versand an den Kunden angestossen (Mail/Portal).
            advance(ticket, TicketState.SENT);
        } else {
            advance(ticket, TicketState.REJECTED);
        }
        return ticket;
    }

    private void advance(Ticket ticket, TicketState state) {
        ticket.setState(state);
        ticketRepository.save(ticket);
        eventPublisher.publish(ticket.getId(), state);
    }
}
