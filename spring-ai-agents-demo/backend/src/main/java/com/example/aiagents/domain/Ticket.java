package com.example.aiagents.domain;

import com.example.aiagents.agent.classification.ClassificationResult;
import com.example.aiagents.agent.research.ResearchResult;
import com.example.aiagents.agent.response.DraftAnswer;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    private UUID id;

    @Column(length = 8000, nullable = false)
    private String customerText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketState state;

    @Convert(converter = Converters.ClassificationResultConverter.class)
    @Column(length = 4000)
    private ClassificationResult classification;

    @Convert(converter = Converters.ResearchResultConverter.class)
    @Column(length = 16000)
    private ResearchResult research;

    @Convert(converter = Converters.DraftAnswerConverter.class)
    @Column(length = 16000)
    private DraftAnswer draft;

    /** Vom Mitarbeiter freigegebener bzw. bearbeiteter Text. */
    @Column(length = 16000)
    private String finalText;

    private Instant createdAt;
    private Instant updatedAt;

    protected Ticket() {
        // JPA
    }

    public static Ticket newTicket(String customerText) {
        Ticket ticket = new Ticket();
        ticket.id = UUID.randomUUID();
        ticket.customerText = customerText;
        ticket.state = TicketState.NEW;
        ticket.createdAt = Instant.now();
        ticket.updatedAt = ticket.createdAt;
        return ticket;
    }

    @PreUpdate
    void touch() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerText() {
        return customerText;
    }

    public TicketState getState() {
        return state;
    }

    public void setState(TicketState state) {
        this.state = state;
        this.updatedAt = Instant.now();
    }

    public ClassificationResult getClassification() {
        return classification;
    }

    public void setClassification(ClassificationResult classification) {
        this.classification = classification;
    }

    public ResearchResult getResearch() {
        return research;
    }

    public void setResearch(ResearchResult research) {
        this.research = research;
    }

    public DraftAnswer getDraft() {
        return draft;
    }

    public void setDraft(DraftAnswer draft) {
        this.draft = draft;
    }

    public String getFinalText() {
        return finalText;
    }

    public void setFinalText(String finalText) {
        this.finalText = finalText;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
