package de.scc.jeebpeldemo.service;

import de.scc.jeebpeldemo.entity.Bestellung;
import de.scc.jeebpeldemo.repository.BestellungRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Fachliche/technische Pruefung einer Bestellung - wird vom BPEL-Prozess
 * (BestellungPruefungWebService) als erster Schritt aufgerufen. Aendert bewusst
 * keinen Status: die Statusfolgetransition (Gelbfall/Auto-/Manager-Freigabe)
 * entscheidet der BPEL-Prozess anhand des Ergebnisses und ruft dafuer den
 * jeweils passenden Partnerservice separat auf.
 */
@Stateless
public class PruefungService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Inject
    private BestellungRepository repository;

    public PruefungsErgebnis pruefen(Long bestellungId) {
        Bestellung bestellung = repository.finden(bestellungId);
        if (bestellung == null) {
            return new PruefungsErgebnis(false, "BESTELLUNG_NICHT_GEFUNDEN");
        }
        List<String> fehlerCodes = new ArrayList<>();
        if (bestellung.getBetrag() == null || bestellung.getBetrag().compareTo(BigDecimal.ZERO) <= 0) {
            fehlerCodes.add("UNGUELTIGER_BETRAG");
        }
        if (bestellung.getKunde() == null
                || bestellung.getKunde().getEmail() == null
                || !EMAIL_PATTERN.matcher(bestellung.getKunde().getEmail()).matches()) {
            fehlerCodes.add("UNGUELTIGE_MAIL_ADRESSE");
        }
        if (!fehlerCodes.isEmpty()) {
            return new PruefungsErgebnis(false, String.join(",", fehlerCodes));
        }
        return new PruefungsErgebnis(true, null);
    }
}
