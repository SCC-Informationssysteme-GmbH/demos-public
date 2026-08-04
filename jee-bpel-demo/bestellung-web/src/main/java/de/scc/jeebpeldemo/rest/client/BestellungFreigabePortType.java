package de.scc.jeebpeldemo.rest.client;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

import java.math.BigDecimal;

/**
 * Client-seitiges SEI, das den PortType von BestellungFreigabeProcess.wsdl
 * (bestellung-bpel) nachbildet - bewusst ohne wsimport/Codegen, sondern per
 * WSDL-loser dynamischer JAX-WS-Bindung (siehe {@link BestellungOdeClient}).
 * Namen/Reihenfolge der Parameter muessen exakt zum Prozess-WSDL passen.
 */
@WebService(name = "BestellungFreigabePortType", targetNamespace = "http://jeebpeldemo.scc.de/bpel/bestellungFreigabe.wsdl")
public interface BestellungFreigabePortType {

    @WebMethod(operationName = "bestellungAnlegen")
    @WebResult(name = "angenommen")
    boolean bestellungAnlegen(
            @WebParam(name = "bestellungId") long bestellungId,
            @WebParam(name = "betrag") BigDecimal betrag);

    @WebMethod(operationName = "bestellungStornieren")
    @WebResult(name = "angenommen")
    boolean bestellungStornieren(
            @WebParam(name = "bestellungId") long bestellungId,
            @WebParam(name = "grund") String grund);

    @WebMethod(operationName = "gelbfallEntscheidung")
    @WebResult(name = "angenommen")
    boolean gelbfallEntscheidung(
            @WebParam(name = "bestellungId") long bestellungId,
            @WebParam(name = "aufgabeId") long aufgabeId,
            @WebParam(name = "freigegeben") boolean freigegeben,
            @WebParam(name = "kommentar") String kommentar);

    @WebMethod(operationName = "managerEntscheidung")
    @WebResult(name = "angenommen")
    boolean managerEntscheidung(
            @WebParam(name = "bestellungId") long bestellungId,
            @WebParam(name = "aufgabeId") long aufgabeId,
            @WebParam(name = "freigegeben") boolean freigegeben,
            @WebParam(name = "kommentar") String kommentar);
}
