package de.scc.jeebpeldemo.it;

import de.scc.jeebpeldemo.it.support.BestellungFixture;
import de.scc.jeebpeldemo.it.support.Poller;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static de.scc.jeebpeldemo.it.support.BestellungFixture.status;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pfad 2: Pruefung schlaegt fehl (Betrag 0) -> Gelbfallbearbeitung -> manuelle
 * Freigabe durch den Sachbearbeiter -> BPEL-Pick liefert die korrelierte Antwort.
 */
class BestellungGelbfallIT {

    @BeforeAll
    static void configure() {
        RestAssured.baseURI = System.getProperty("it.wildflyBaseUri", "http://localhost:9081/bestellung");
    }

    @Test
    void ungueltigeBestellungDurchlaeuftGelbfallUndWirdFreigegeben() throws Exception {
        long id = BestellungFixture.legeBestellungAnUndWarteAufPruefung("Gelbfall IT", "gelbfall-it@example.com", "0");

        given().contentType(ContentType.JSON).when().post("/api/bestellungen/" + id + "/prozess/start").then().statusCode(200);

        Poller.waitUntil(() -> status(id), s -> "GELBFALL".equals(s), Duration.ofSeconds(15), Duration.ofMillis(300));

        long aufgabeId = BestellungFixture.findeOffeneAufgabeId("gelbfall", id);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"aufgabeId": %d, "freigegeben": true, "kommentar": "IT-Test Gelbfall"}
                        """.formatted(aufgabeId))
                .when()
                .post("/api/bestellungen/" + id + "/prozess/gelbfall-entscheidung")
                .then()
                .statusCode(200)
                .body("angenommen", org.hamcrest.Matchers.equalTo(true));

        String status = Poller.waitUntil(() -> status(id), s -> "FREIGEGEBEN".equals(s), Duration.ofSeconds(15), Duration.ofMillis(300));
        assertEquals("FREIGEGEBEN", status);
    }
}
