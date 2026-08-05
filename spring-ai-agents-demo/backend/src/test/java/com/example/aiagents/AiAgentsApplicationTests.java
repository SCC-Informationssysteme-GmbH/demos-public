package com.example.aiagents;

import com.example.aiagents.agent.Category;
import com.example.aiagents.domain.Ticket;
import com.example.aiagents.domain.TicketState;
import com.example.aiagents.orchestration.CustomerNotificationService;
import com.example.aiagents.orchestration.WorkflowPlan;
import com.example.aiagents.orchestration.WorkflowPlanRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

@SpringBootTest
@TestPropertySource(properties = "spring.ai.openai.api-key=test-key")
class AiAgentsApplicationTests {

    @Autowired
    private WorkflowPlanRegistry registry;

    @Autowired
    private CustomerNotificationService notificationService;

    @Test
    void contextLoads() {
        assertThat(registry).isNotNull();
    }

    @Test
    void vertragsfrageWirdRecherchiertAberNichtAutomatischEntworfen() {
        WorkflowPlan plan = registry.planFor(Category.VERTRAGSFRAGE);

        assertThat(plan.runResearch()).isTrue();
        assertThat(plan.runResponseDraft()).isFalse();
        assertThat(plan.terminalStateIfSkipped()).isEqualTo(TicketState.ESCALATED);
    }

    @Test
    void featureWunschLandetImBacklogUndDerKundeErhaeltEineBestaetigung() {
        WorkflowPlan plan = registry.planFor(Category.FEATURE_WUNSCH);

        assertThat(plan.runResearch()).isFalse();
        assertThat(plan.runResponseDraft()).isFalse();
        assertThat(plan.terminalStateIfSkipped()).isEqualTo(TicketState.LOGGED);
        assertThat(plan.sendAutoAcknowledgement()).isTrue();
    }

    /**
     * Die Sicherheitseigenschaft des Entwurfs: ohne Freigabe darf nur ein Vorlagentext
     * an den Kunden gehen. Ein Plan, der gleichzeitig einen LLM-Entwurf erzeugt und
     * ungefragt verschickt, wuerde die Freigabe aushebeln.
     */
    @Test
    void keinPlanVerschicktEinenLlmEntwurfOhneFreigabe() {
        for (Category category : Category.values()) {
            WorkflowPlan plan = registry.planFor(category);

            assertThat(plan.runResponseDraft() && plan.sendAutoAcknowledgement())
                    .as("Kategorie %s: Entwurf UND Auto-Versand", category)
                    .isFalse();
        }
    }

    /**
     * Die Pfade ohne Entwurf schulden dem Kunden eine Rueckmeldung, die Pfade mit Entwurf
     * bekommen sie durch die Freigabe.
     */
    @Test
    void nurDiePfadeOhneEntwurfVerschickenAutomatisch() {
        assertThat(registry.planFor(Category.FEATURE_WUNSCH).sendAutoAcknowledgement()).isTrue();
        assertThat(registry.planFor(Category.VERTRAGSFRAGE).sendAutoAcknowledgement()).isTrue();
        assertThat(registry.planFor(Category.TECHNISCHES_PROBLEM).sendAutoAcknowledgement()).isFalse();
        assertThat(registry.planFor(Category.SONSTIGES).sendAutoAcknowledgement()).isFalse();
    }

    /**
     * Der Kunde darf nie ohne jede Rueckmeldung bleiben: entweder es entsteht ein Entwurf
     * (der freigegeben wird) oder eine Vorlage geht raus.
     */
    @Test
    void jedeKategorieFuehrtZuEntwurfOderBestaetigung() {
        for (Category category : Category.values()) {
            WorkflowPlan plan = registry.planFor(category);

            assertThat(plan.runResponseDraft() || plan.sendAutoAcknowledgement())
                    .as("Kategorie %s: weder Entwurf noch Bestaetigung", category)
                    .isTrue();
        }
    }

    @Test
    void unbekannteKategorieFaelltAufVollenPfadZurueck() {
        WorkflowPlan plan = registry.planFor(null);

        assertThat(plan.runResearch()).isTrue();
        assertThat(plan.runResponseDraft()).isTrue();
        assertThat(plan.terminalStateIfSkipped()).isNull();
        assertThat(plan.sendAutoAcknowledgement()).isFalse();
    }

    /**
     * Jeder sendende Plan braucht eine Vorlage, in der der Platzhalter wirklich ersetzt
     * wird - ein unersetztes {ticketId} ginge sonst so an den Kunden.
     */
    @Test
    void jedeSendendeKategorieHatEineVorlageMitAufgeloesterReferenz() {
        Ticket ticket = Ticket.newTicket("Testanfrage");

        for (Category category : Category.values()) {
            if (!registry.planFor(category).sendAutoAcknowledgement()) {
                continue;
            }
            String text = notificationService.acknowledgement(ticket, category);

            assertThat(text)
                    .as("Vorlage fuer %s", category)
                    .contains(ticket.getId().toString())
                    .doesNotContain("{ticketId}")
                    .isNotBlank();
        }
    }

    @Test
    void ohneVorlageWirdLautstarkGescheitertStattStillNichtsZuSenden() {
        Ticket ticket = Ticket.newTicket("Testanfrage");

        assertThatIllegalStateException()
                .isThrownBy(() -> notificationService.acknowledgement(
                        ticket, Category.TECHNISCHES_PROBLEM))
                .withMessageContaining("TECHNISCHES_PROBLEM");
    }
}
