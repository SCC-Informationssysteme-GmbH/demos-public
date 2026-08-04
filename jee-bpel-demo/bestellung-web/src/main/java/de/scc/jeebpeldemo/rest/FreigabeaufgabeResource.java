package de.scc.jeebpeldemo.rest;

import de.scc.jeebpeldemo.entity.Freigabeaufgabe;
import de.scc.jeebpeldemo.service.BestellungFreigabeService;
import de.scc.jeebpeldemo.service.GelbfallService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * Arbeitsvorrat-Abfragen fuer die Sachbearbeiter-/Manager-GUI (siehe bestellung-web JSF-Views).
 */
@Path("/freigabeaufgaben")
@Produces(MediaType.APPLICATION_JSON)
public class FreigabeaufgabeResource {

    @Inject
    private GelbfallService gelbfallService;

    @Inject
    private BestellungFreigabeService freigabeService;

    @GET
    @Path("/gelbfall")
    public List<Freigabeaufgabe> offeneGelbfaelle() {
        return gelbfallService.offeneGelbfaelle();
    }

    @GET
    @Path("/manager")
    public List<Freigabeaufgabe> offeneManagerFreigaben() {
        return freigabeService.offeneManagerFreigaben();
    }
}
