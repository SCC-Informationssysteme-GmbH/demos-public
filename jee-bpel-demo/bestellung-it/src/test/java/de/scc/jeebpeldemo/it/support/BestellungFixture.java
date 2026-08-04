package de.scc.jeebpeldemo.it.support;

import io.restassured.http.ContentType;

import java.time.Duration;

import static io.restassured.RestAssured.given;

/**
 * Legt eine Bestellung per REST an und wartet, bis die MDB sie asynchron
 * (ueber JMS) auf Status IN_PRUEFUNG gesetzt hat. Setzt voraus, dass
 * RestAssured.baseURI bereits vom aufrufenden Test konfiguriert wurde.
 */
public final class BestellungFixture {

    private BestellungFixture() {
    }

    public static long legeBestellungAnUndWarteAufPruefung(String kundenName, String kundenEmail, String betrag) throws InterruptedException {
        long id = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"kunde": {"name": "%s", "email": "%s"}, "betrag": %s, "positionen": []}
                        """.formatted(kundenName, kundenEmail, betrag))
                .when()
                .post("/api/bestellungen")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        Poller.waitUntil(
                () -> given().when().get("/api/bestellungen/" + id).then().extract().jsonPath().getString("status"),
                status -> "IN_PRUEFUNG".equals(status),
                Duration.ofSeconds(10),
                Duration.ofMillis(300)
        );

        return id;
    }

    public static String status(long bestellungId) {
        return given().when().get("/api/bestellungen/" + bestellungId).then().extract().jsonPath().getString("status");
    }

    public static long findeOffeneAufgabeId(String typPfad, long bestellungId) throws InterruptedException {
        return Poller.waitUntil(
                () -> given().when().get("/api/freigabeaufgaben/" + typPfad).then().extract().jsonPath()
                        .getLong("find { it.bestellungId == " + bestellungId + " }.id"),
                id -> id != null,
                Duration.ofSeconds(10),
                Duration.ofMillis(300)
        );
    }
}
