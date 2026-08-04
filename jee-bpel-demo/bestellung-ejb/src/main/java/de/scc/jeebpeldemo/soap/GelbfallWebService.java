package de.scc.jeebpeldemo.soap;

import de.scc.jeebpeldemo.service.GelbfallService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jws.Oneway;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

/**
 * SOAP-Partnerservice fuer BestellungFreigabeProcess.bpel (Scope "Gelbfall"
 * und die Eskalations-Scope "EskalationAlsGelbfall").
 */
@Stateless
@WebService(
        serviceName = "GelbfallService",
        portName = "GelbfallWebServicePort",
        targetNamespace = "http://jeebpeldemo.scc.de/gelbfall"
)
public class GelbfallWebService {

    @Inject
    private GelbfallService gelbfallService;

    @WebMethod
    @WebResult(name = "gelbfallId")
    public long gelbfallEroeffnen(
            @WebParam(name = "bestellungId") long bestellungId,
            @WebParam(name = "grund") String grund) {
        return gelbfallService.gelbfallEroeffnen(bestellungId, grund);
    }

    @WebMethod
    @Oneway
    public void gelbfallSchliessen(
            @WebParam(name = "bestellungId") long bestellungId,
            @WebParam(name = "gelbfallId") long gelbfallId,
            @WebParam(name = "grund") String grund) {
        gelbfallService.gelbfallSchliessen(bestellungId, gelbfallId, grund);
    }
}
