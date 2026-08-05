package com.example.aiagents.orchestration;

import com.example.aiagents.agent.Category;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Vorlagen fuer die Texte, die ohne Mitarbeiter-Freigabe an den Kunden gehen -
 * je Kategorie eine. Waechst damit an derselben Stelle wie die
 * {@link WorkflowPlanRegistry}: neue Kategorie, neuer Eintrag.
 *
 * @param acknowledgements Kategorie -> Vorlagentext, Platzhalter {@code {ticketId}}
 */
@ConfigurationProperties(prefix = "app.notifications")
public record NotificationProperties(Map<Category, String> acknowledgements) {

    public NotificationProperties {
        acknowledgements = acknowledgements == null ? Map.of() : Map.copyOf(acknowledgements);
    }
}
