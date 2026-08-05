package com.example.aiagents.orchestration;

import com.example.aiagents.agent.Category;
import com.example.aiagents.domain.TicketState;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Zuordnung Kategorie -> Ablaufplan. Eine neue Kategorie oder ein neuer Pfad
 * bedeutet genau einen neuen Map-Eintrag; der Orchestrator bleibt unveraendert.
 */
@Component
public class WorkflowPlanRegistry {

    private static final WorkflowPlan FULL_PIPELINE =
            new WorkflowPlan(true, true, null, false);

    private final Map<Category, WorkflowPlan> plans = Map.of(
            Category.TECHNISCHES_PROBLEM, FULL_PIPELINE,
            // Vertragsfrage: recherchieren (Vertragsdaten), KEIN Auto-Entwurf -> Fachabteilung,
            // der Kunde erfaehrt per Vorlage, dass die Anfrage dort liegt
            Category.VERTRAGSFRAGE, new WorkflowPlan(true, false, TicketState.ESCALATED, true),
            // Feature-Wunsch: weder Recherche noch Entwurf noetig -> ins Backlog, aber der
            // Kunde bekommt eine feste Eingangsbestaetigung (ohne Freigabe, siehe WorkflowPlan)
            Category.FEATURE_WUNSCH, new WorkflowPlan(false, false, TicketState.LOGGED, true),
            Category.SONSTIGES, FULL_PIPELINE);

    public WorkflowPlan planFor(Category category) {
        if (category == null) {
            return FULL_PIPELINE;
        }
        return plans.getOrDefault(category, FULL_PIPELINE);
    }
}
