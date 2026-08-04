package de.scc.jeebpeldemo.web;

import de.scc.jeebpeldemo.entity.Freigabeaufgabe;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Named
@ViewScoped
public class GelbfallQueueBean implements Serializable {

    private static final String CODE_UNGUELTIGER_BETRAG = "UNGUELTIGER_BETRAG";
    private static final String CODE_UNGUELTIGE_MAIL_ADRESSE = "UNGUELTIGE_MAIL_ADRESSE";

    @Inject
    private ApiClient apiClient;

    private List<Freigabeaufgabe> aufgaben;

    /**
     * IDs der Freigabeaufgaben, deren zugrundeliegende Bestellung waehrend dieser
     * Session ueber den Bearbeiten-Dialog korrigiert wurde - der urspruengliche
     * "Grund" bleibt als Historie stehen, die Freigabe soll aber wieder moeglich sein.
     */
    private Set<Long> korrigierteAufgabenIds = new HashSet<>();

    private Freigabeaufgabe bearbeiteteAufgabe;
    private BigDecimal bearbeiterBetrag;
    private String bearbeiteteEmail;

    public void init() {
        aufgaben = apiClient.offeneGelbfaelle();
        aufgaben.sort(Comparator.comparing(Freigabeaufgabe::getBestellungId).reversed());
    }

    public List<Freigabeaufgabe> getAufgaben() {
        if (aufgaben == null) {
            init();
        }
        return aufgaben;
    }

    public boolean istKorrigiert(Freigabeaufgabe aufgabe) {
        return korrigierteAufgabenIds.contains(aufgabe.getId());
    }

    /**
     * Der Grund kann bei mehreren gleichzeitig ungueltigen Feldern mehrere
     * Fehlercodes kommagetrennt enthalten (siehe PruefungService.pruefen).
     */
    public boolean hatFehler(Freigabeaufgabe aufgabe, String code) {
        return aufgabe != null && aufgabe.getGrund() != null && aufgabe.getGrund().contains(code);
    }

    public void bearbeiten(Freigabeaufgabe aufgabe) {
        bearbeiteteAufgabe = aufgabe;
        bearbeiterBetrag = aufgabe.getBestellungBetrag();
        bearbeiteteEmail = aufgabe.getBestellungKundeEmail();
    }

    public void speichern() {
        if (bearbeiteteAufgabe == null) {
            return;
        }
        if (hatFehler(bearbeiteteAufgabe, CODE_UNGUELTIGER_BETRAG)) {
            apiClient.bestellungBetragKorrigieren(bearbeiteteAufgabe.getBestellungId(), bearbeiterBetrag);
        }
        if (hatFehler(bearbeiteteAufgabe, CODE_UNGUELTIGE_MAIL_ADRESSE)) {
            apiClient.bestellungEmailKorrigieren(bearbeiteteAufgabe.getBestellungId(), bearbeiteteEmail);
        }
        korrigierteAufgabenIds.add(bearbeiteteAufgabe.getId());
        bearbeiteteAufgabe = null;
        init();
        hinweis("Bestellung korrigiert.");
    }

    public Freigabeaufgabe getBearbeiteteAufgabe() {
        return bearbeiteteAufgabe;
    }

    public BigDecimal getBearbeiterBetrag() {
        return bearbeiterBetrag;
    }

    public void setBearbeiterBetrag(BigDecimal bearbeiterBetrag) {
        this.bearbeiterBetrag = bearbeiterBetrag;
    }

    public String getBearbeiteteEmail() {
        return bearbeiteteEmail;
    }

    public void setBearbeiteteEmail(String bearbeiteteEmail) {
        this.bearbeiteteEmail = bearbeiteteEmail;
    }

    public void freigeben(Freigabeaufgabe aufgabe, String kommentar) {
        try {
            apiClient.gelbfallEntscheiden(aufgabe.getBestellungId(), aufgabe.getId(), true, kommentar);
            hinweis("Gelbfall freigegeben.");
        } catch (WebApplicationException e) {
            fehler("Freigabe fuer Bestellung " + aufgabe.getBestellungId() + " wurde vom Prozess abgelehnt - "
                    + "die Instanz wartet vermutlich nicht mehr (z.B. bereits per Timeout final entschieden).");
        }
        init();
    }

    public void ablehnen(Freigabeaufgabe aufgabe, String kommentar) {
        try {
            apiClient.gelbfallEntscheiden(aufgabe.getBestellungId(), aufgabe.getId(), false, kommentar);
            hinweis("Gelbfall abgelehnt.");
        } catch (WebApplicationException e) {
            fehler("Ablehnung fuer Bestellung " + aufgabe.getBestellungId() + " wurde vom Prozess abgelehnt - "
                    + "die Instanz wartet vermutlich nicht mehr (z.B. bereits per Timeout final entschieden).");
        }
        init();
    }

    private void hinweis(String text) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, text, null));
    }

    private void fehler(String text) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, text, null));
    }
}
