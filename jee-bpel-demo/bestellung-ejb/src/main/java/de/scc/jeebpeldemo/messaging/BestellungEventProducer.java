package de.scc.jeebpeldemo.messaging;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;

@Stateless
public class BestellungEventProducer {

    @Inject
    private JMSContext jmsContext;

    @Resource(lookup = "java:/jms/queue/OrderEventsQueue")
    private Queue orderEventsQueue;

    public void bestellungAngelegt(Long bestellungId) {
        jmsContext.createProducer().send(orderEventsQueue, bestellungId);
    }
}
