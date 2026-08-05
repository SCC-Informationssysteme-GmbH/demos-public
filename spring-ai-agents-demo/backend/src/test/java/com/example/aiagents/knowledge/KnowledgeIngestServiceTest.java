package com.example.aiagents.knowledge;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Braucht ein laufendes Qdrant (docker compose up -d).
 */
@SpringBootTest
@TestPropertySource(properties = "spring.ai.openai.api-key=test-key")
class KnowledgeIngestServiceTest {

    @Autowired
    private KnowledgeIngestService ingestService;

    /**
     * Sichert die Zusage "Aenderungen an den Wissensquellen ohne Neustart" ab: gelesen
     * werden muss die Datei im Quellbaum, nicht die Kopie unter target/classes. Kippt die
     * Reihenfolge der Locations, liest der Reindex wieder die veraltete Kopie - und das
     * faellt im Betrieb erst auf, wenn eine Aenderung scheinbar wirkungslos bleibt.
     */
    @Test
    void liestDieWissensquellenAusDemQuellbaumUndNichtAusTargetClasses() throws IOException {
        IngestResult result = ingestService.reindex();

        assertThat(result.files()).isPositive();
        assertThat(result.sources()).allSatisfy(source ->
                assertThat(source.origin())
                        .as("Herkunft von %s", source.source())
                        .contains("/src/main/resources/knowledge/")
                        .doesNotContain("target/classes"));
    }

    /**
     * Der Endpoint darf beliebig oft aufgerufen werden - zweimal indexieren muss dasselbe
     * Ergebnis liefern, sonst waechst die Collection mit jedem Aufruf.
     */
    @Test
    void zweimalIndexierenErgibtDieselbeChunkAnzahl() throws IOException {
        IngestResult first = ingestService.reindex();
        IngestResult second = ingestService.reindex();

        assertThat(second.files()).isEqualTo(first.files());
        assertThat(second.chunks()).isEqualTo(first.chunks());
    }

    /**
     * Ohne Front Matter muss eine Quelle als INTERNAL gelten. Der Test prueft das am
     * echten Ingest, nicht nur am Parser.
     */
    @Test
    void sichtbarkeitUndKategorieLandenAlsMetadatenImIndex() throws IOException {
        IngestResult result = ingestService.reindex();

        assertThat(result.sources())
                .allSatisfy(source -> assertThat(source.audience())
                        .isIn(KnowledgeMetadata.AUDIENCE_CUSTOMER, KnowledgeMetadata.AUDIENCE_INTERNAL))
                .anySatisfy(source -> assertThat(source.audience())
                        .isEqualTo(KnowledgeMetadata.AUDIENCE_CUSTOMER));
    }
}
