package de.scc.jeebpeldemo.service;

import de.scc.jeebpeldemo.entity.Bestellung;
import de.scc.jeebpeldemo.entity.BestellungStatus;
import de.scc.jeebpeldemo.repository.BestellungRepository;
import de.scc.jeebpeldemo.repository.FreigabeaufgabeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BestellungFreigabeServiceTest {

    @Mock
    private BestellungRepository bestellungRepository;

    @Mock
    private FreigabeaufgabeRepository aufgabeRepository;

    @InjectMocks
    private BestellungFreigabeService freigabeService;

    @Test
    void automatischFreigebenSchlaegtFehlWennNichtInPruefung() {
        Bestellung bestellung = new Bestellung();
        bestellung.setId(1L);
        bestellung.setStatus(BestellungStatus.NEU);
        when(bestellungRepository.finden(1L)).thenReturn(bestellung);

        boolean freigegeben = freigabeService.automatischFreigeben(1L);

        assertFalse(freigegeben);
        assertEquals(BestellungStatus.NEU, bestellung.getStatus());
    }

    @Test
    void automatischFreigebenSetztStatusFreigegeben() {
        Bestellung bestellung = new Bestellung();
        bestellung.setId(1L);
        bestellung.setStatus(BestellungStatus.IN_PRUEFUNG);
        when(bestellungRepository.finden(1L)).thenReturn(bestellung);

        boolean freigegeben = freigabeService.automatischFreigeben(1L);

        assertTrue(freigegeben);
        assertEquals(BestellungStatus.FREIGEGEBEN, bestellung.getStatus());
    }

    @Test
    void managerFreigabeEskalierenMarkiertAufgabeUndSetztStatus() {
        freigabeService.managerFreigabeEskalieren(2L, 99L);

        verify(aufgabeRepository).eskalieren(99L);
        verify(bestellungRepository).statusAktualisieren(2L, BestellungStatus.ESKALIERT);
    }

    @Test
    void kompensationFreigebenNimmtFreigabeZurueckWennFreigegeben() {
        Bestellung bestellung = new Bestellung();
        bestellung.setId(1L);
        bestellung.setStatus(BestellungStatus.FREIGEGEBEN);
        when(bestellungRepository.finden(1L)).thenReturn(bestellung);

        freigabeService.kompensationFreigeben(1L);

        assertEquals(BestellungStatus.STORNIERT, bestellung.getStatus());
    }

    @Test
    void kompensationFreigebenIstNoopWennNichtFreigegeben() {
        Bestellung bestellung = new Bestellung();
        bestellung.setId(1L);
        bestellung.setStatus(BestellungStatus.MANAGER_FREIGABE);
        when(bestellungRepository.finden(1L)).thenReturn(bestellung);

        freigabeService.kompensationFreigeben(1L);

        assertEquals(BestellungStatus.MANAGER_FREIGABE, bestellung.getStatus());
    }
}
