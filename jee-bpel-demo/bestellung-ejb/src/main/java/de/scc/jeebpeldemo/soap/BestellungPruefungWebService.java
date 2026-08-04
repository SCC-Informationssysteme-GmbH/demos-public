package de.scc.jeebpeldemo.soap;

import de.scc.jeebpeldemo.service.PruefungService;
import de.scc.jeebpeldemo.service.PruefungsErgebnis;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.ws.Holder;

/**
 * SOAP-Partnerservice fuer BestellungFreigabeProcess.bpel (Scope "Pruefung").
 * Zwei Rueckgabewerte (gueltig, fehlerCode) werden per JAX-WS-Holder-OUT-Parameter
 * uebertragen - laut WS-I Basic Profile / JAX-WS-Spezifikation immer in der
 * Reihenfolge Rueckgabewert zuerst, dann OUT-Parameter in Deklarationsreihenfolge,
 * daher deckungsgleich mit dem handgeschriebenen WSDL in bestellung-bpel.
 */
@Stateless
@WebService(
        serviceName = "BestellungPruefungService",
        portName = "BestellungPruefungWebServicePort",
        targetNamespace = "http://jeebpeldemo.scc.de/bestellungPruefung"
)
public class BestellungPruefungWebService {

    @Inject
    private PruefungService pruefungService;

    @WebMethod
    @WebResult(name = "gueltig")
    public boolean pruefeBestellung(
            @WebParam(name = "bestellungId") long bestellungId,
            @WebParam(name = "fehlerCode", mode = WebParam.Mode.OUT) Holder<String> fehlerCode) {
        PruefungsErgebnis ergebnis = pruefungService.pruefen(bestellungId);
        fehlerCode.value = ergebnis.getFehlerCode();
        return ergebnis.isGueltig();
    }
}
