package de.scc.jeebpeldemo.service;

import de.scc.jeebpeldemo.entity.Bestellung;
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
 * Freigabepfade jenseits des Gelbfalls: automatische Freigabe (Betrag &lt; 10000)
 * und Manager-Freigabe inkl. Eskalation (Betrag &gt;= 10000), sowie der zentrale
 * Status-Ruecksprung aus dem BPEL-Prozess (System of Record bleibt WildFly/Oracle).
 */
@Stateless
public class BestellungFreigabeService {

    @Inject
    private BestellungRepository bestellungRepository;

    @Inject
    private FreigabeaufgabeRepository aufgabeRepository;

    public boolean automatischFreigeben(Long bestellungId) {
        Bestellung bestellung = bestellungRepository.finden(bestellungId);
        if (bestellung == null || bestellung.getStatus() != BestellungStatus.IN_PRUEFUNG) {
            return false;
        }
        bestellung.setStatus(BestellungStatus.FREIGEGEBEN);
        return true;
    }

    public Long managerFreigabeAnfordern(Long bestellungId) {
        Freigabeaufgabe aufgabe = new Freigabeaufgabe();
        aufgabe.setBestellung(bestellungRepository.finden(bestellungId));
        aufgabe.setTyp(FreigabeaufgabeTyp.MANAGER);
        aufgabe.setGrund("Bestellbetrag >= 10000 - Vier-Augen-Freigabe erforderlich");
        aufgabe.setErstelltAm(LocalDateTime.now());
        aufgabeRepository.anlegen(aufgabe);
        bestellungRepository.statusAktualisieren(bestellungId, BestellungStatus.MANAGER_FREIGABE);
        return aufgabe.getId();
    }

    /**
     * Wird vom BPEL-Prozess bei Ueberschreiten des Eskalations-Timeouts
     * (onAlarm im pick der ManagerFreigabe-Scope) aufgerufen.
     */
    public void managerFreigabeEskalieren(Long bestellungId, Long aufgabeId) {
        aufgabeRepository.eskalieren(aufgabeId);
        bestellungRepository.statusAktualisieren(bestellungId, BestellungStatus.ESKALIERT);
    }

    /**
     * Wird von der REST-Fassade beim Eintreffen der Managerentscheidung
     * aufgerufen - lokaler Schreibvorgang, unabhaengig vom SOAP-Ruecksprung zu ODE.
     */
    public void managerEntscheidungEintragen(Long aufgabeId, Entscheidung entscheidung, String kommentar) {
        aufgabeRepository.entscheiden(aufgabeId, entscheidung, kommentar);
    }

    /**
     * Kompensation einer bereits erteilten Freigabe (Auto- oder Manager-Pfad),
     * ausgeloest durch den compensationHandler beim Stornieren einer laufenden
     * Prozessinstanz. Demo-Umfang: Status zurueknehmen; in einem echten System
     * wuerden hier z.B. reservierte Bestandsmengen freigegeben.
     */
    public void kompensationFreigeben(Long bestellungId) {
        Bestellung bestellung = bestellungRepository.finden(bestellungId);
        if (bestellung != null && bestellung.getStatus() == BestellungStatus.FREIGEGEBEN) {
            bestellung.setStatus(BestellungStatus.STORNIERT);
        }
    }

    /**
     * Zentraler Status-Schreiber, den der BPEL-Prozess nach jedem Teilschritt
     * aufruft (System-of-Record-Sync, siehe BestellungFreigabeProcess.bpel).
     */
    public void statusAktualisieren(Long bestellungId, BestellungStatus status) {
        bestellungRepository.statusAktualisieren(bestellungId, status);
    }

    public List<Freigabeaufgabe> offeneManagerFreigaben() {
        return aufgabeRepository.findenOffeneNachTyp(FreigabeaufgabeTyp.MANAGER);
    }
}
