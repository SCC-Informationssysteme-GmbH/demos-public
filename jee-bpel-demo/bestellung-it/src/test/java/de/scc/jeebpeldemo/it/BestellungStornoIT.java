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
 * Abbruch-Pattern: waehrend eine Gelbfallbearbeitung noch offen ist, storniert
 * der Kunde die Bestellung. Prueft eventHandlers + Fault-basiertes Abbrechen
 * eines gerade in einem <pick> wartenden Prozesses.
 */
class BestellungStornoIT {

    @BeforeAll
    static void configure() {
        RestAssured.baseURI = System.getProperty("it.wildflyBaseUri", "http://localhost:9081/bestellung");
    }

    @Test
    void bestellungKannWaehrendGelbfallStorniertWerden() throws Exception {
        long id = BestellungFixture.legeBestellungAnUndWarteAufPruefung("Storno IT", "storno-it@example.com", "0");

        given().contentType(ContentType.JSON).when().post("/api/bestellungen/" + id + "/prozess/start").then().statusCode(200);

        Poller.waitUntil(() -> status(id), s -> "GELBFALL".equals(s), Duration.ofSeconds(15), Duration.ofMillis(300));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"grund": "IT-Test Storno"}
                        """)
                .when()
                .post("/api/bestellungen/" + id + "/prozess/stornieren")
                .then()
                .statusCode(200)
                .body("angenommen", org.hamcrest.Matchers.equalTo(true));

        String status = Poller.waitUntil(() -> status(id), s -> "STORNIERT".equals(s), Duration.ofSeconds(15), Duration.ofMillis(300));
        assertEquals("STORNIERT", status);
    }
}
