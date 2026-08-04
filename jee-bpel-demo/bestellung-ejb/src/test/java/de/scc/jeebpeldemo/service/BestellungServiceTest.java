package de.scc.jeebpeldemo.service;

import de.scc.jeebpeldemo.entity.Bestellung;
import de.scc.jeebpeldemo.entity.BestellungStatus;
import de.scc.jeebpeldemo.messaging.BestellungEventProducer;
import de.scc.jeebpeldemo.repository.BestellungRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BestellungServiceTest {

    @Mock
    private BestellungRepository repository;

    @Mock
    private BestellungEventProducer eventProducer;

    @InjectMocks
    private BestellungService bestellungService;

    @Test
    void anlegenSetztStatusNeuUndSendetEvent() {
        Bestellung bestellung = new Bestellung();
        bestellung.setId(42L);

        Bestellung ergebnis = bestellungService.anlegen(bestellung);

        assertEquals(BestellungStatus.NEU, ergebnis.getStatus());
        verify(repository).anlegen(bestellung);
        verify(eventProducer).bestellungAngelegt(42L);
    }
}
