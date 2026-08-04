package de.scc.jeebpeldemo.service;

import de.scc.jeebpeldemo.entity.Bestellung;
import de.scc.jeebpeldemo.entity.Kunde;
import de.scc.jeebpeldemo.repository.BestellungRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PruefungServiceTest {

    @Mock
    private BestellungRepository repository;

    @InjectMocks
    private PruefungService pruefungService;

    private Bestellung gueltigeBestellung() {
        Kunde kunde = new Kunde();
        kunde.setEmail("max@example.com");
        Bestellung bestellung = new Bestellung();
        bestellung.setId(1L);
        bestellung.setKunde(kunde);
        bestellung.setBetrag(new BigDecimal("500.00"));
        return bestellung;
    }

    @Test
    void pruefenLiefertFehlerWennBestellungNichtGefunden() {
        when(repository.finden(1L)).thenReturn(null);

        PruefungsErgebnis ergebnis = pruefungService.pruefen(1L);

        assertFalse(ergebnis.isGueltig());
        assertEquals("BESTELLUNG_NICHT_GEFUNDEN", ergebnis.getFehlerCode());
    }

    @Test
    void pruefenLiefertFehlerBeiUngueltigemBetrag() {
        Bestellung bestellung = gueltigeBestellung();
        bestellung.setBetrag(BigDecimal.ZERO);
        when(repository.finden(1L)).thenReturn(bestellung);

        PruefungsErgebnis ergebnis = pruefungService.pruefen(1L);

        assertFalse(ergebnis.isGueltig());
        assertEquals("UNGUELTIGER_BETRAG", ergebnis.getFehlerCode());
    }

    @Test
    void pruefenLiefertFehlerBeiUngueltigerKundenEmail() {
        Bestellung bestellung = gueltigeBestellung();
        bestellung.getKunde().setEmail("keine-email");
        when(repository.finden(1L)).thenReturn(bestellung);

        PruefungsErgebnis ergebnis = pruefungService.pruefen(1L);

        assertFalse(ergebnis.isGueltig());
        assertEquals("UNGUELTIGE_MAIL_ADRESSE", ergebnis.getFehlerCode());
    }

    @Test
    void pruefenLiefertBeideFehlerBeiUngueltigemBetragUndEmail() {
        Bestellung bestellung = gueltigeBestellung();
        bestellung.setBetrag(BigDecimal.ZERO);
        bestellung.getKunde().setEmail("keine-email");
        when(repository.finden(1L)).thenReturn(bestellung);

        PruefungsErgebnis ergebnis = pruefungService.pruefen(1L);

        assertFalse(ergebnis.isGueltig());
        assertEquals("UNGUELTIGER_BETRAG,UNGUELTIGE_MAIL_ADRESSE", ergebnis.getFehlerCode());
    }

    @Test
    void pruefenLiefertGueltigWennAllesPasst() {
        when(repository.finden(1L)).thenReturn(gueltigeBestellung());

        PruefungsErgebnis ergebnis = pruefungService.pruefen(1L);

        assertTrue(ergebnis.isGueltig());
    }
}
