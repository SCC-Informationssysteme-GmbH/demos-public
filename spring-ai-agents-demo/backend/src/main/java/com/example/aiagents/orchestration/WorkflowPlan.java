package com.example.aiagents.orchestration;

import com.example.aiagents.domain.TicketState;

/**
 * Ablaufplan fuer eine Kategorie (docs/01, Abschnitt 3.3.1).
 *
 * @param runResearch              Recherche-Agent aufrufen?
 * @param runResponseDraft         Antwort-Agent aufrufen? Sein Entwurf geht immer in die
 *                                 Mitarbeiter-Freigabe.
 * @param terminalStateIfSkipped   Endzustand, wenn kein Entwurf erstellt wird (sonst null)
 * @param sendAutoAcknowledgement  Feste Eingangsbestaetigung ohne Freigabe verschicken?
 *                                 Nur zulaessig, weil der Text aus einer Vorlage kommt und
 *                                 nicht vom LLM - eine ungeprueft generierte Nachricht an
 *                                 den Kunden waere genau das, was die Freigabe verhindern
 *                                 soll.
 */
public record WorkflowPlan(boolean runResearch,
                           boolean runResponseDraft,
                           TicketState terminalStateIfSkipped,
                           boolean sendAutoAcknowledgement) {
}
