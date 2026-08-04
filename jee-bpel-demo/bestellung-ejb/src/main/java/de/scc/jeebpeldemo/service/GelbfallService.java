package de.scc.jeebpeldemo.service;

import de.scc.jeebpeldemo.entity.BestellungStatus;
import de.scc.jeebpeldemo.entity.Entscheidung;
import de.scc.jeebpeldemo.entity.Freigabeaufgabe;
import de.scc.jeebpeldemo.entity.FreigabeaufgabeTyp;
import de.scc.jeebpeldemo.repository.BestellungRepository;
import de.scc.jeebpeldemo.repository.FreigabeaufgabeRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Gelbfallbearbeitung: Sachbearbeiter-Arbeitsvorrat fuer Bestellungen, deren
 * Pruefung fehlgeschlagen ist, oder fuer eskalierte Manager-Freigaben
 * (siehe {@link BestellungFreigabeService#managerFreigabeEskalieren}).
 */
@Stateless
public class GelbfallService {

    @Inject
    private FreigabeaufgabeRepository aufgabeRepository;

    @Inject
    private BestellungRepository bestellungRepository;

    public Long gelbfallEroeffnen(Long bestellungId, String grund) {
        Freigabeaufgabe aufgabe = new Freigabeaufgabe();
        aufgabe.setBestellung(bestellungRepository.finden(bestellungId));
        aufgabe.setTyp(FreigabeaufgabeTyp.GELBFALL);
        aufgabe.setGrund(grund);
        aufgabe.setErstelltAm(LocalDateTime.now());
        aufgabeRepository.anlegen(aufgabe);
        bestellungRepository.statusAktualisieren(bestellungId, BestellungStatus.GELBFALL);
        return aufgabe.getId();
    }

    /**
     * Aufraeumen ohne explizite Sachbearbeiterentscheidung - z.B. wenn die
     * Bestellung waehrend offener Gelbfallbearbeitung storniert wird, oder wenn
     * der finale Fallback-Timeout der Eskalations-Scope (onAlarm PT1M in
     * BestellungFreigabeProcess.bpel) die Bestellung bereits automatisch
     * abgelehnt hat. Ohne diesen Aufruf bliebe die Freigabeaufgabe dauerhaft
     * "offen", obwohl der BPEL-Prozess laengst beendet ist und eine spaetere
     * Freigeben/Ablehnen-Entscheidung dafuer nur noch mit einem ODE-Fehler
     * fehlschlagen kann.
     */
    public void gelbfallSchliessen(Long bestellungId, Long gelbfallId, String grund) {
        Freigabeaufgabe aufgabe = aufgabeRepository.finden(gelbfallId);
        if (aufgabe != null && !aufgabe.isErledigt()) {
            aufgabe.setErledigt(true);
            aufgabe.setKommentar(grund);
        }
    }

    /**
     * Wird von der REST-Fassade beim Eintreffen der Sachbearbeiterentscheidung
     * aufgerufen - lokaler Schreibvorgang, unabhaengig vom SOAP-Ruecksprung zu ODE.
     */
    public void entscheidungEintragen(Long aufgabeId, Entscheidung entscheidung, String kommentar) {
        aufgabeRepository.entscheiden(aufgabeId, entscheidung, kommentar);
    }

    public List<Freigabeaufgabe> offeneGelbfaelle() {
        return aufgabeRepository.findenOffeneNachTyp(FreigabeaufgabeTyp.GELBFALL);
    }
}
