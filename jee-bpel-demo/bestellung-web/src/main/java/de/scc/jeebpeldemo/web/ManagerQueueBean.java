package de.scc.jeebpeldemo.web;

import de.scc.jeebpeldemo.entity.Freigabeaufgabe;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

@Named
@ViewScoped
public class ManagerQueueBean implements Serializable {

    @Inject
    private ApiClient apiClient;

    private List<Freigabeaufgabe> aufgaben;

    public void init() {
        aufgaben = apiClient.offeneManagerFreigaben();
        aufgaben.sort(Comparator.comparing(Freigabeaufgabe::getBestellungId).reversed());
    }

    public List<Freigabeaufgabe> getAufgaben() {
        if (aufgaben == null) {
            init();
        }
        return aufgaben;
    }

    public void freigeben(Freigabeaufgabe aufgabe, String kommentar) {
        try {
            apiClient.managerEntscheiden(aufgabe.getBestellungId(), aufgabe.getId(), true, kommentar);
            hinweis("Manager-Freigabe erteilt.");
        } catch (WebApplicationException e) {
            fehler("Freigabe fuer Bestellung " + aufgabe.getBestellungId() + " wurde vom Prozess abgelehnt - "
                    + "die Instanz wartet vermutlich nicht mehr (z.B. bereits per Timeout eskaliert).");
        }
        init();
    }

    public void ablehnen(Freigabeaufgabe aufgabe, String kommentar) {
        try {
            apiClient.managerEntscheiden(aufgabe.getBestellungId(), aufgabe.getId(), false, kommentar);
            hinweis("Manager-Freigabe abgelehnt.");
        } catch (WebApplicationException e) {
            fehler("Ablehnung fuer Bestellung " + aufgabe.getBestellungId() + " wurde vom Prozess abgelehnt - "
                    + "die Instanz wartet vermutlich nicht mehr (z.B. bereits per Timeout eskaliert).");
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
