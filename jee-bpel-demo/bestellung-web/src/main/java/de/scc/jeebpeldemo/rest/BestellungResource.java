package de.scc.jeebpeldemo.rest;

import de.scc.jeebpeldemo.entity.Bestellung;
import de.scc.jeebpeldemo.entity.BestellungStatus;
import de.scc.jeebpeldemo.repository.BestellungSuchfilter;
import de.scc.jeebpeldemo.rest.dto.BestellungKorrekturRequest;
import de.scc.jeebpeldemo.service.BestellungService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;

@Path("/bestellungen")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BestellungResource {

    @Inject
    private BestellungService bestellungService;

    @POST
    public Response anlegen(Bestellung bestellung) {
        Bestellung angelegt = bestellungService.anlegen(bestellung);
        return Response.status(Response.Status.CREATED).entity(angelegt).build();
    }

    @GET
    @Path("/{id}")
    public Response finden(@PathParam("id") Long id) {
        Bestellung bestellung = bestellungService.finden(id);
        if (bestellung == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(bestellung).build();
    }

    @PATCH
    @Path("/{id}")
    public Response korrigieren(@PathParam("id") Long id, BestellungKorrekturRequest request) {
        Bestellung bestellung = bestellungService.korrigieren(id, request.getBetrag(), request.getEmail());
        if (bestellung == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(bestellung).build();
    }

    @GET
    public List<Bestellung> suchen(
            @QueryParam("kundeId") Long kundeId,
            @QueryParam("status") BestellungStatus status,
            @QueryParam("von") String von,
            @QueryParam("bis") String bis
    ) {
        BestellungSuchfilter filter = new BestellungSuchfilter();
        filter.setKundeId(kundeId);
        filter.setStatus(status);
        if (von != null) {
            filter.setVon(LocalDateTime.parse(von));
        }
        if (bis != null) {
            filter.setBis(LocalDateTime.parse(bis));
        }
        return bestellungService.suchen(filter);
    }
}
