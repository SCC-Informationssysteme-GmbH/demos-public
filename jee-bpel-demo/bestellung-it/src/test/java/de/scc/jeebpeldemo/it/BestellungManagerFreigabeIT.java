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
 * Pfad 3: valide Bestellung, Betrag &gt;= 10000 -> Manager-Freigabe
 * (Vier-Augen-Prinzip). Testet die Entscheidung vor Ablauf des Eskalations-Timeouts (PT2M).
 */
class BestellungManagerFreigabeIT {

    @BeforeAll
    static void configure() {
        RestAssured.baseURI = System.getProperty("it.wildflyBaseUri", "http://localhost:9081/bestellung");
    }

    @Test
    void bestellungAb10000BrauchtManagerFreigabe() throws Exception {
        long id = BestellungFixture.legeBestellungAnUndWarteAufPruefung("Manager IT", "manager-it@example.com", "15000.00");

        given().contentType(ContentType.JSON).when().post("/api/bestellungen/" + id + "/prozess/start").then().statusCode(200);

        Poller.waitUntil(() -> status(id), s -> "MANAGER_FREIGABE".equals(s), Duration.ofSeconds(15), Duration.ofMillis(300));

        long aufgabeId = BestellungFixture.findeOffeneAufgabeId("manager", id);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"aufgabeId": %d, "freigegeben": true, "kommentar": "IT-Test Manager"}
                        """.formatted(aufgabeId))
                .when()
                .post("/api/bestellungen/" + id + "/prozess/manager-entscheidung")
                .then()
                .statusCode(200)
                .body("angenommen", org.hamcrest.Matchers.equalTo(true));

        String status = Poller.waitUntil(() -> status(id), s -> "FREIGEGEBEN".equals(s), Duration.ofSeconds(15), Duration.ofMillis(300));
        assertEquals("FREIGEGEBEN", status);
    }
}
