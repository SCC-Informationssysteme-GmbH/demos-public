package de.scc.jeebpeldemo.it;

import de.scc.jeebpeldemo.it.support.BestellungFixture;
import de.scc.jeebpeldemo.it.support.Poller;
import de.scc.jeebpeldemo.it.support.SoapClient;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static de.scc.jeebpeldemo.it.support.BestellungFixture.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Belegt den dritten geforderten Zugriffsweg: direkter SOAP-Aufruf gegen den
 * von Apache ODE exponierten Prozessendpunkt, ganz ohne REST-Fassade/GUI.
 */
class BestellungFreigabeProcessDirectSoapIT {

    private static String odeEndpoint;

    @BeforeAll
    static void configure() {
        RestAssured.baseURI = System.getProperty("it.wildflyBaseUri", "http://localhost:9081/bestellung");
        odeEndpoint = System.getProperty("it.odeEndpoint",
                "http://localhost:8181/ode/processes/BestellungFreigabeProcess");
    }

    @Test
    void prozessKannDirektPerSoapAnOdeGestartetWerden() throws Exception {
        long id = BestellungFixture.legeBestellungAnUndWarteAufPruefung("Direct SOAP IT", "direct-soap-it@example.com", "250.00");

        String soapRequest = """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                  <soapenv:Body>
                    <bestellungAnlegen xmlns="http://jeebpeldemo.scc.de/bpel/bestellungFreigabe.wsdl">
                      <bestellungId xmlns="">%d</bestellungId>
                      <betrag xmlns="">250.00</betrag>
                    </bestellungAnlegen>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(id);

        String soapResponse = SoapClient.post(odeEndpoint, soapRequest);
        assertTrue(Boolean.parseBoolean(SoapClient.extractElementText(soapResponse, "angenommen")),
                "ODE sollte den Prozessstart per direktem SOAP annehmen: " + soapResponse);

        String status = Poller.waitUntil(() -> status(id), s -> "FREIGEGEBEN".equals(s), Duration.ofSeconds(15), Duration.ofMillis(300));
        assertEquals("FREIGEGEBEN", status);
    }
}
