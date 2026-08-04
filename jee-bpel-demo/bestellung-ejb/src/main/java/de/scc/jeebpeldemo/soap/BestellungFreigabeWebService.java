package de.scc.jeebpeldemo.soap;

import de.scc.jeebpeldemo.entity.BestellungStatus;
import de.scc.jeebpeldemo.service.BestellungFreigabeService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jws.Oneway;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

/**
 * SOAP-Partnerservice fuer BestellungFreigabeProcess.bpel: automatische Freigabe
 * (Betrag &lt; 10000), Manager-Freigabe-Anforderung/Eskalation (Betrag &gt;= 10000),
 * Kompensation sowie der zentrale Status-Ruecksprung nach jedem Teilschritt.
 */
@Stateless
@WebService(
        serviceName = "BestellungFreigabeService",
        portName = "BestellungFreigabeWebServicePort",
        targetNamespace = "http://jeebpeldemo.scc.de/bestellungFreigabe"
)
public class BestellungFreigabeWebService {

    @Inject
    private BestellungFreigabeService freigabeService;

    @WebMethod
    @WebResult(name = "freigegeben")
    public boolean automatischFreigeben(@WebParam(name = "bestellungId") long bestellungId) {
        return freigabeService.automatischFreigeben(bestellungId);
    }

    @WebMethod
    @WebResult(name = "aufgabeId")
    public long managerFreigabeAnfordern(@WebParam(name = "bestellungId") long bestellungId) {
        return freigabeService.managerFreigabeAnfordern(bestellungId);
    }

    @WebMethod
    @Oneway
    public void managerFreigabeEskalieren(
            @WebParam(name = "bestellungId") long bestellungId,
            @WebParam(name = "aufgabeId") long aufgabeId) {
        freigabeService.managerFreigabeEskalieren(bestellungId, aufgabeId);
    }

    @WebMethod
    @Oneway
    public void kompensationFreigeben(@WebParam(name = "bestellungId") long bestellungId) {
        freigabeService.kompensationFreigeben(bestellungId);
    }

    @WebMethod
    @Oneway
    public void statusAktualisieren(
            @WebParam(name = "bestellungId") long bestellungId,
            @WebParam(name = "status") String status) {
        freigabeService.statusAktualisieren(bestellungId, BestellungStatus.valueOf(status));
    }
}
