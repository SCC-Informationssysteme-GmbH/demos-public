package de.scc.jeebpeldemo.service;

import de.scc.jeebpeldemo.entity.Bestellung;
import de.scc.jeebpeldemo.entity.BestellungStatus;
import de.scc.jeebpeldemo.messaging.BestellungEventProducer;
import de.scc.jeebpeldemo.repository.BestellungRepository;
import de.scc.jeebpeldemo.repository.BestellungSuchfilter;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class BestellungService {

    @Inject
    private BestellungRepository repository;

    @Inject
    private BestellungEventProducer eventProducer;

    public Bestellung anlegen(Bestellung bestellung) {
        bestellung.setBestelldatum(LocalDateTime.now());
        bestellung.setStatus(BestellungStatus.NEU);
        repository.anlegen(bestellung);
        eventProducer.bestellungAngelegt(bestellung.getId());
        return bestellung;
    }

    public Bestellung finden(Long id) {
        return repository.finden(id);
    }

    public List<Bestellung> suchen(BestellungSuchfilter filter) {
        return repository.suchen(filter);
    }

    /**
     * Korrigiert Betrag und/oder Kunden-Email einer Bestellung - genutzt aus der
     * Gelbfall-GUI, wenn die Pruefung wegen UNGUELTIGER_BETRAG/UNGUELTIGE_MAIL_ADRESSE
     * fehlgeschlagen ist und der Sachbearbeiter den Wert vor der Freigabe berichtigt.
     */
    public Bestellung korrigieren(Long bestellungId, BigDecimal betrag, String email) {
        Bestellung bestellung = repository.finden(bestellungId);
        if (bestellung == null) {
            return null;
        }
        if (betrag != null) {
            bestellung.setBetrag(betrag);
        }
        if (email != null && bestellung.getKunde() != null) {
            bestellung.getKunde().setEmail(email);
        }
        return bestellung;
    }
}
