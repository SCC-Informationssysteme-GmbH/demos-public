package de.scc.jeebpeldemo.web;

import de.scc.jeebpeldemo.entity.Bestellung;
import de.scc.jeebpeldemo.entity.BestellungStatus;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class BestellungenBean implements Serializable {

    @Inject
    private ApiClient apiClient;

    private List<Bestellung> bestellungen;

    /**
     * Bestellungs-IDs, fuer die in dieser Session bereits Prozess starten/Stornieren
     * ausgeloest wurde. Noetig, weil der REST-Aufruf nur auf die sofortige BPEL-Ack-
     * Reply wartet - die eigentliche Statusaenderung (Pruefung/Verzweigung) laeuft in
     * ODE asynchron weiter und ist beim direkt anschliessenden init() oft noch nicht
     * sichtbar. Ohne dieses Tracking bliebe der Button bis zum naechsten (zufaellig
     * spaeteren) Reload klickbar und man koennte denselben Prozess mehrfach starten.
     */
    private Set<Long> aktionAusgeloestFuer = new HashSet<>();

    public void init() {
        bestellungen = apiClient.bestellungen();
        bestellungen.sort(Comparator.comparing(Bestellung::getId).reversed());
    }

    public List<Bestellung> getBestellungen() {
        if (bestellungen == null) {
            init();
        }
        return bestellungen;
    }

    public List<SelectItem> getStatusOptionen() {
        return Arrays.stream(BestellungStatus.values())
                .map(status -> new SelectItem(status, status.name()))
                .collect(Collectors.toList());
    }

    public boolean istAktionAusgeloest(Long bestellungId) {
        return aktionAusgeloestFuer.contains(bestellungId);
    }

    public void prozessStarten(Long bestellungId) {
        apiClient.prozessStarten(bestellungId);
        aktionAusgeloestFuer.add(bestellungId);
        init();
        hinweis("Freigabeprozess gestartet.");
    }

    public void prozessStornieren(Long bestellungId) {
        apiClient.prozessStornieren(bestellungId, "Vom Sachbearbeiter storniert");
        aktionAusgeloestFuer.add(bestellungId);
        init();
        hinweis("Stornierung angestossen.");
    }

    private void hinweis(String text) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, text, null));
    }
}
