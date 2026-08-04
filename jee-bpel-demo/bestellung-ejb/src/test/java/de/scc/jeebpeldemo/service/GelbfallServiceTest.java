package de.scc.jeebpeldemo.service;

import de.scc.jeebpeldemo.entity.Bestellung;
import de.scc.jeebpeldemo.entity.BestellungStatus;
import de.scc.jeebpeldemo.entity.Entscheidung;
import de.scc.jeebpeldemo.entity.FreigabeaufgabeTyp;
import de.scc.jeebpeldemo.repository.BestellungRepository;
import de.scc.jeebpeldemo.repository.FreigabeaufgabeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.scc.jeebpeldemo.entity.Freigabeaufgabe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GelbfallServiceTest {

    @Mock
    private FreigabeaufgabeRepository aufgabeRepository;

    @Mock
    private BestellungRepository bestellungRepository;

    @InjectMocks
    private GelbfallService gelbfallService;

    @Test
    void gelbfallEroeffnenLegtAufgabeAnUndSetztStatus() {
        Bestellung bestellung = new Bestellung();
        bestellung.setId(7L);
        when(bestellungRepository.finden(7L)).thenReturn(bestellung);

        gelbfallService.gelbfallEroeffnen(7L, "Pruefung fehlgeschlagen");

        ArgumentCaptor<Freigabeaufgabe> captor = ArgumentCaptor.forClass(Freigabeaufgabe.class);
        verify(aufgabeRepository).anlegen(captor.capture());
        assertEquals(FreigabeaufgabeTyp.GELBFALL, captor.getValue().getTyp());
        assertEquals("Pruefung fehlgeschlagen", captor.getValue().getGrund());
        verify(bestellungRepository).statusAktualisieren(7L, BestellungStatus.GELBFALL);
    }

    @Test
    void entscheidungEintragenDelegiertAnRepository() {
        gelbfallService.entscheidungEintragen(3L, Entscheidung.FREIGEGEBEN, "passt");

        verify(aufgabeRepository).entscheiden(3L, Entscheidung.FREIGEGEBEN, "passt");
    }
}
