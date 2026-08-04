package de.scc.jeebpeldemo.messaging;

import de.scc.jeebpeldemo.entity.BestellungStatus;
import de.scc.jeebpeldemo.repository.BestellungRepository;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/OrderEventsQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
})
public class BestellungEventConsumer implements MessageListener {

    @Inject
    private BestellungRepository repository;

    @Override
    public void onMessage(Message message) {
        try {
            Long bestellungId = message.getBody(Long.class);
            repository.statusAktualisieren(bestellungId, BestellungStatus.IN_PRUEFUNG);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
