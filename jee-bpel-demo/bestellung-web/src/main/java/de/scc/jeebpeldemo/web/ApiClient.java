package de.scc.jeebpeldemo.web;

import de.scc.jeebpeldemo.entity.Bestellung;
import de.scc.jeebpeldemo.entity.Freigabeaufgabe;
import de.scc.jeebpeldemo.rest.BestellungProcessResource.ProzessAntwort;
import de.scc.jeebpeldemo.rest.dto.BestellungKorrekturRequest;
import de.scc.jeebpeldemo.rest.dto.EntscheidungRequest;
import de.scc.jeebpeldemo.rest.dto.StornoRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Buendelt die REST-Aufrufe der JSF-Backing-Beans gegen die eigene REST-Fassade
 * (bestellung-web/rest). Die GUI ruft dieselbe REST-API auf, die auch direkt per
 * curl/Postman erreichbar ist - kein Sonderweg fuer den Browser-Client.
 */
@ApplicationScoped
public class ApiClient {

    private final Client client = ClientBuilder.newClient();

    private String baseUrl() {
        return System.getProperty("app.base.url", "http://localhost:8080/bestellung/api");
    }

    private WebTarget target(String path) {
        return client.target(baseUrl()).path(path);
    }

    public List<Bestellung> bestellungen() {
        return target("/bestellungen").request(MediaType.APPLICATION_JSON).get(new GenericType<List<Bestellung>>() {
        });
    }

    public Bestellung anlegen(Bestellung bestellung) {
        return target("/bestellungen").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(bestellung, MediaType.APPLICATION_JSON), Bestellung.class);
    }

    public ProzessAntwort prozessStarten(Long bestellungId) {
        return target("/bestellungen/" + bestellungId + "/prozess/start")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(null), ProzessAntwort.class);
    }

    public ProzessAntwort prozessStornieren(Long bestellungId, String grund) {
        StornoRequest request = new StornoRequest();
        request.setGrund(grund);
        return target("/bestellungen/" + bestellungId + "/prozess/stornieren")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON), ProzessAntwort.class);
    }

    public List<Freigabeaufgabe> offeneGelbfaelle() {
        return target("/freigabeaufgaben/gelbfall").request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Freigabeaufgabe>>() {
                });
    }

    public List<Freigabeaufgabe> offeneManagerFreigaben() {
        return target("/freigabeaufgaben/manager").request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Freigabeaufgabe>>() {
                });
    }

    public Bestellung bestellungBetragKorrigieren(Long bestellungId, BigDecimal betrag) {
        BestellungKorrekturRequest request = new BestellungKorrekturRequest();
        request.setBetrag(betrag);
        return target("/bestellungen/" + bestellungId).request(MediaType.APPLICATION_JSON)
                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON), Bestellung.class);
    }

    public Bestellung bestellungEmailKorrigieren(Long bestellungId, String email) {
        BestellungKorrekturRequest request = new BestellungKorrekturRequest();
        request.setEmail(email);
        return target("/bestellungen/" + bestellungId).request(MediaType.APPLICATION_JSON)
                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON), Bestellung.class);
    }

    public ProzessAntwort gelbfallEntscheiden(Long bestellungId, Long aufgabeId, boolean freigegeben, String kommentar) {
        return entscheidung(bestellungId, "gelbfall-entscheidung", aufgabeId, freigegeben, kommentar);
    }

    public ProzessAntwort managerEntscheiden(Long bestellungId, Long aufgabeId, boolean freigegeben, String kommentar) {
        return entscheidung(bestellungId, "manager-entscheidung", aufgabeId, freigegeben, kommentar);
    }

    private ProzessAntwort entscheidung(Long bestellungId, String pfad, Long aufgabeId, boolean freigegeben, String kommentar) {
        EntscheidungRequest request = new EntscheidungRequest();
        request.setAufgabeId(aufgabeId);
        request.setFreigegeben(freigegeben);
        request.setKommentar(kommentar);
        return target("/bestellungen/" + bestellungId + "/prozess/" + pfad)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON), ProzessAntwort.class);
    }
}
