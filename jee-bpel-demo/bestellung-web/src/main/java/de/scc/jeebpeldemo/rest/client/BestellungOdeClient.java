package de.scc.jeebpeldemo.rest.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPBinding;

import javax.xml.namespace.QName;
import java.math.BigDecimal;

/**
 * Duenner JAX-WS-Client gegen den von Apache ODE exponierten SOAP-Endpunkt von
 * BestellungFreigabeProcess. Bewusst WSDL-los gebunden (Service.create(QName) +
 * addPort statt wsimport-Codegen): das SEI {@link BestellungFreigabePortType}
 * bildet den Prozess-Contract 1:1 nach, ein Build-Time-Codegen-Schritt entfaellt.
 *
 * Bewusst KEIN gecachter/geteilter Port: JAX-WS-Client-Proxys (CXF-basiert unter
 * WildFly) sind nicht dafuer ausgelegt, von mehreren Request-Threads gleichzeitig
 * wiederverwendet zu werden - ein geteilter Proxy fuehrte im Test zu haengenden
 * Aufrufen ohne jede Server-seitige Spur bei ODE. Ein frischer Port pro Aufruf
 * kostet etwas Overhead, ist aber garantiert threadsicher.
 */
@ApplicationScoped
public class BestellungOdeClient {

    private static final String NAMESPACE = "http://jeebpeldemo.scc.de/bpel/bestellungFreigabe.wsdl";
    private static final QName SERVICE_NAME = new QName(NAMESPACE, "BestellungFreigabeProcessService");
    private static final QName PORT_NAME = new QName(NAMESPACE, "BestellungFreigabePort");

    private BestellungFreigabePortType port() {
        String baseUrl = System.getProperty("ode.base.url", "http://ode:8080/ode");
        String endpointAddress = baseUrl + "/processes/BestellungFreigabeProcess";
        Service service = Service.create(SERVICE_NAME);
        service.addPort(PORT_NAME, SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);
        return service.getPort(PORT_NAME, BestellungFreigabePortType.class);
    }

    public boolean starten(long bestellungId, BigDecimal betrag) {
        try {
            return port().bestellungAnlegen(bestellungId, betrag);
        } catch (WebServiceException e) {
            throw new OdeProzessException("Prozessstart fuer Bestellung " + bestellungId + " fehlgeschlagen", e);
        }
    }

    public boolean stornieren(long bestellungId, String grund) {
        try {
            return port().bestellungStornieren(bestellungId, grund);
        } catch (WebServiceException e) {
            throw new OdeProzessException("Stornierung fuer Bestellung " + bestellungId + " fehlgeschlagen", e);
        }
    }

    public boolean gelbfallEntscheidungMelden(long bestellungId, long aufgabeId, boolean freigegeben, String kommentar) {
        try {
            return port().gelbfallEntscheidung(bestellungId, aufgabeId, freigegeben, kommentar);
        } catch (WebServiceException e) {
            throw new OdeProzessException("Gelbfallentscheidung fuer Bestellung " + bestellungId + " fehlgeschlagen", e);
        }
    }

    public boolean managerEntscheidungMelden(long bestellungId, long aufgabeId, boolean freigegeben, String kommentar) {
        try {
            return port().managerEntscheidung(bestellungId, aufgabeId, freigegeben, kommentar);
        } catch (WebServiceException e) {
            throw new OdeProzessException("Managerentscheidung fuer Bestellung " + bestellungId + " fehlgeschlagen", e);
        }
    }
}
