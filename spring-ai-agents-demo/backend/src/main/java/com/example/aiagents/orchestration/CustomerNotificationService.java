package com.example.aiagents.orchestration;

import com.example.aiagents.agent.Category;
import com.example.aiagents.domain.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Erzeugt die Texte, die <b>ohne</b> Mitarbeiter-Freigabe an den Kunden gehen.
 *
 * <p>Bewusst reine Vorlagen-Ersetzung und kein LLM-Aufruf: nur weil der Wortlaut
 * vorab festgelegt ist, darf die Nachricht die Freigabe umgehen. Sobald hier ein
 * Modell generieren wuerde, muesste der Text wieder durch die Freigabe.
 */
@Service
public class CustomerNotificationService {

    private static final Logger log = LoggerFactory.getLogger(CustomerNotificationService.class);
    private static final String TICKET_ID_PLACEHOLDER = "{ticketId}";

    private final NotificationProperties properties;

    /**
     * Prueft beim Start, dass Plan und Vorlagen zusammenpassen. Ohne diese Pruefung waere
     * ein Tippfehler im Kategorie-Schluessel ein stiller Ausfall: der Plan wuerde
     * verschicken wollen, es gaebe keinen Text, und der Kunde bekaeme nichts - auffallen
     * wuerde das erst im Betrieb.
     */
    public CustomerNotificationService(NotificationProperties properties,
                                      WorkflowPlanRegistry registry) {
        this.properties = properties;

        List<Category> missing = new ArrayList<>();
        for (Category category : Category.values()) {
            if (registry.planFor(category).sendAutoAcknowledgement()
                    && !properties.acknowledgements().containsKey(category)) {
                missing.add(category);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Kein Vorlagentext unter app.notifications.acknowledgements fuer: " + missing);
        }

        properties.acknowledgements().keySet().stream()
                .filter(category -> !registry.planFor(category).sendAutoAcknowledgement())
                .forEach(category -> log.warn(
                        "Vorlage fuer {} vorhanden, aber der Plan verschickt nicht - toter Eintrag",
                        category));
    }

    /**
     * @throws IllegalStateException wenn fuer die Kategorie keine Vorlage konfiguriert ist.
     *                               Lieber lautstark scheitern als den Kunden ohne
     *                               Rueckmeldung lassen.
     */
    public String acknowledgement(Ticket ticket, Category category) {
        String template = properties.acknowledgements().get(category);
        if (template == null) {
            throw new IllegalStateException("Keine Vorlage fuer Kategorie " + category);
        }
        return template.replace(TICKET_ID_PLACEHOLDER, ticket.getId().toString());
    }
}
