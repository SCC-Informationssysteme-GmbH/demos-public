package de.scc.jeebpeldemo.web;

import de.scc.jeebpeldemo.entity.Bestellung;
import de.scc.jeebpeldemo.entity.Kunde;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;

@Named
@ViewScoped
public class NeueBestellungBean implements Serializable {

    @Inject
    private ApiClient apiClient;

    private String kundeName;
    private String kundeEmail;
    private BigDecimal betrag;

    /**
     * Ueber f:event type="preRenderView" gebunden statt @PostConstruct: die
     * Meldung muss VOR dem Rendern der p:messages-Komponente in der FacesContext
     * stehen, @PostConstruct feuert aber erst, wenn ein spaeteres Tag im Baum
     * (z.B. das erste Eingabefeld) den Bean-EL-Ausdruck zum ersten Mal aufloest.
     */
    public void hinweisAnzeigen() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (!context.isPostback()) {
            // Nicht-globale Client-ID: nur die lokale p:messages auf dieser Seite soll den
            // Hinweis zeigen, nicht auch das globalOnly-Panel im Template.
            context.addMessage("neueBestellungForm", new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Demo-Hinweis: Sie koennen absichtlich eine ungueltige E-Mail-Adresse oder einen "
                            + "ungueltigen Betrag (<= 0) eingeben - der Prozess erkennt dies bei der Pruefung "
                            + "und steuert die Bestellung automatisch in die Gelbfallbearbeitung aus.",
                    null));
        }
    }

    public String getKundeName() {
        return kundeName;
    }

    public void setKundeName(String kundeName) {
        this.kundeName = kundeName;
    }

    public String getKundeEmail() {
        return kundeEmail;
    }

    public void setKundeEmail(String kundeEmail) {
        this.kundeEmail = kundeEmail;
    }

    public BigDecimal getBetrag() {
        return betrag;
    }

    public void setBetrag(BigDecimal betrag) {
        this.betrag = betrag;
    }

    public String anlegen() {
        Kunde kunde = new Kunde();
        kunde.setName(kundeName);
        kunde.setEmail(kundeEmail);

        Bestellung bestellung = new Bestellung();
        bestellung.setKunde(kunde);
        bestellung.setBetrag(betrag == null ? BigDecimal.ZERO : betrag);

        apiClient.anlegen(bestellung);

        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Bestellung angelegt.", null));
        return "/bestellungen?faces-redirect=true";
    }
}
