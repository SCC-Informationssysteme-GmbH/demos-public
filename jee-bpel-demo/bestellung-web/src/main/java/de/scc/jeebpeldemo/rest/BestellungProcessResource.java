package de.scc.jeebpeldemo.rest;

import de.scc.jeebpeldemo.entity.Bestellung;
import de.scc.jeebpeldemo.entity.Entscheidung;
import de.scc.jeebpeldemo.rest.client.BestellungOdeClient;
import de.scc.jeebpeldemo.rest.client.OdeProzessException;
import de.scc.jeebpeldemo.rest.dto.EntscheidungRequest;
import de.scc.jeebpeldemo.rest.dto.StornoRequest;
import de.scc.jeebpeldemo.service.BestellungFreigabeService;
import de.scc.jeebpeldemo.service.BestellungService;
import de.scc.jeebpeldemo.service.GelbfallService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST-Fassade Richtung Apache ODE: GUI und externe REST-Clients starten/
 * stornieren den BPEL-Prozess und melden Gelbfall-/Managerentscheidungen
 * ausschliesslich ueber diese Endpunkte - direkter SOAP-Zugriff auf ODE
 * bleibt daneben moeglich (siehe docs/02-architektur.md).
 */
@Path("/bestellungen/{id}/prozess")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BestellungProcessResource {

    @Inject
    private BestellungService bestellungService;

    @Inject
    private GelbfallService gelbfallService;

    @Inject
    private BestellungFreigabeService freigabeService;

    @Inject
    private BestellungOdeClient odeClient;

    @POST
    @Path("/start")
    public Response start(@PathParam("id") Long id) {
        Bestellung bestellung = bestellungService.finden(id);
        if (bestellung == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            boolean angenommen = odeClient.starten(id, bestellung.getBetrag());
            return Response.ok(new ProzessAntwort(angenommen)).build();
        } catch (OdeProzessException e) {
            return Response.status(Response.Status.BAD_GATEWAY).entity(new FehlerAntwort(e.getMessage())).build();
        }
    }

    @POST
    @Path("/stornieren")
    public Response stornieren(@PathParam("id") Long id, StornoRequest request) {
        try {
            boolean angenommen = odeClient.stornieren(id, request == null ? null : request.getGrund());
            return Response.ok(new ProzessAntwort(angenommen)).build();
        } catch (OdeProzessException e) {
            return Response.status(Response.Status.CONFLICT).entity(new FehlerAntwort(e.getMessage())).build();
        }
    }

    @POST
    @Path("/gelbfall-entscheidung")
    public Response gelbfallEntscheidung(@PathParam("id") Long id, EntscheidungRequest request) {
        try {
            boolean angenommen = odeClient.gelbfallEntscheidungMelden(
                    id, request.getAufgabeId(), request.isFreigegeben(), request.getKommentar());
            // Erst nach erfolgreicher Zustellung an ODE lokal als erledigt markieren -
            // sonst koennte die Aufgabe als erledigt gelten, obwohl der Prozess noch wartet.
            Entscheidung entscheidung = request.isFreigegeben() ? Entscheidung.FREIGEGEBEN : Entscheidung.ABGELEHNT;
            gelbfallService.entscheidungEintragen(request.getAufgabeId(), entscheidung, request.getKommentar());
            return Response.ok(new ProzessAntwort(angenommen)).build();
        } catch (OdeProzessException e) {
            return Response.status(Response.Status.CONFLICT).entity(new FehlerAntwort(e.getMessage())).build();
        }
    }

    @POST
    @Path("/manager-entscheidung")
    public Response managerEntscheidung(@PathParam("id") Long id, EntscheidungRequest request) {
        try {
            boolean angenommen = odeClient.managerEntscheidungMelden(
                    id, request.getAufgabeId(), request.isFreigegeben(), request.getKommentar());
            Entscheidung entscheidung = request.isFreigegeben() ? Entscheidung.FREIGEGEBEN : Entscheidung.ABGELEHNT;
            freigabeService.managerEntscheidungEintragen(request.getAufgabeId(), entscheidung, request.getKommentar());
            return Response.ok(new ProzessAntwort(angenommen)).build();
        } catch (OdeProzessException e) {
            return Response.status(Response.Status.CONFLICT).entity(new FehlerAntwort(e.getMessage())).build();
        }
    }

    public static class ProzessAntwort {
        private boolean angenommen;

        public ProzessAntwort() {
        }

        public ProzessAntwort(boolean angenommen) {
            this.angenommen = angenommen;
        }

        public boolean isAngenommen() {
            return angenommen;
        }

        public void setAngenommen(boolean angenommen) {
            this.angenommen = angenommen;
        }
    }

    public static class FehlerAntwort {
        private String fehler;

        public FehlerAntwort() {
        }

        public FehlerAntwort(String fehler) {
            this.fehler = fehler;
        }

        public String getFehler() {
            return fehler;
        }

        public void setFehler(String fehler) {
            this.fehler = fehler;
        }
    }
}
