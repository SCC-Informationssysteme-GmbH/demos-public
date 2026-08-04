package de.scc.jeebpeldemo.it;

import de.scc.jeebpeldemo.it.support.BestellungFixture;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static de.scc.jeebpeldemo.it.support.BestellungFixture.status;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pfad 1: valide Bestellung, Betrag &lt; 10000 -> automatische Freigabe ohne
 * manuelle Entscheidung. Deckt REST-Anlage -> BPEL-Prozessstart -> Ergebnis ab.
 */
class BestellungAutoFreigabeIT {

    @BeforeAll
    static void configure() {
        RestAssured.baseURI = System.getProperty("it.wildflyBaseUri", "http://localhost:9081/bestellung");
    }

    @Test
    void bestellungUnter10000WirdAutomatischFreigegeben() throws Exception {
        long id = BestellungFixture.legeBestellungAnUndWarteAufPruefung("Auto Freigabe IT", "auto-it@example.com", "500.00");

        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/bestellungen/" + id + "/prozess/start")
                .then()
                .statusCode(200)
                .body("angenommen", org.hamcrest.Matchers.equalTo(true));

        String status = de.scc.jeebpeldemo.it.support.Poller.waitUntil(
                () -> status(id),
                s -> "FREIGEGEBEN".equals(s),
                Duration.ofSeconds(15),
                Duration.ofMillis(300));

        assertEquals("FREIGEGEBEN", status);
    }
}
