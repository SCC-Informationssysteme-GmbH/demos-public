package com.example.aiagents.knowledge;

import com.example.aiagents.agent.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueft den Sichtbarkeitsschutz auf der <b>Retrieval</b>-Ebene.
 *
 * <p>Diese Tests fehlten und das war der Grund, warum ein Defekt lange unentdeckt blieb:
 * geprueft wurde nur, welche Quellen das LLM am Ende zitiert. Das sagt nichts darueber, was
 * es zu sehen bekam - ein zu weiter Filter fiel nicht auf, weil das Modell die
 * unpassenden Dokumente von sich aus ignorierte.
 *
 * <p>Braucht ein laufendes Qdrant (docker compose up -d).
 */
@SpringBootTest
@TestPropertySource(properties = "spring.ai.openai.api-key=test-key")
class KnowledgeFilterTest {

    private static final String QUERY = "Vertrag kuendigen";

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private KnowledgeIngestService ingestService;

    @BeforeEach
    void indexieren() throws Exception {
        ingestService.reindex();
    }

    private List<Document> search(String filter) {
        SearchRequest.Builder builder = SearchRequest.builder().query(QUERY).topK(10);
        if (filter != null) {
            builder.filterExpression(filter);
        }
        return vectorStore.similaritySearch(builder.build());
    }

    /** Basis: ohne Filter sind alle Quellen erreichbar - sonst testet der Rest nichts. */
    @Test
    void ohneFilterSindAlleQuellenErreichbar() {
        assertThat(search(null))
                .extracting(hit -> hit.getMetadata().get(KnowledgeMetadata.CATEGORY))
                .contains("VERTRAGSFRAGE", "TECHNISCHES_PROBLEM", KnowledgeMetadata.GENERAL_CATEGORY);
    }

    /**
     * Der Kern: der Filter des ResearchAgent muss fremde Kategorien ausschliessen. Vor dem
     * Fix lieferte er alle drei Dokumente, weil ein geklammerter Ausdruck von Spring AI
     * still verworfen wurde.
     */
    @Test
    void derFilterDesAgentenSchliesstFremdeKategorienAus() {
        List<Document> hits = search(
                KnowledgeFilter.forCustomerFacingSearch(Category.VERTRAGSFRAGE));

        assertThat(hits).isNotEmpty();
        assertThat(hits).allSatisfy(hit -> assertThat(
                hit.getMetadata().get(KnowledgeMetadata.CATEGORY))
                .isIn("VERTRAGSFRAGE", KnowledgeMetadata.GENERAL_CATEGORY));
        assertThat(hits)
                .extracting(hit -> hit.getMetadata().get(KnowledgeMetadata.SOURCE))
                .doesNotContain("faq-login.md");
    }

    /** Der Sichtbarkeitsteil muss fuer jede Kategorie greifen, nicht nur fuer eine. */
    @Test
    void nurFuerKundenFreigegebeneQuellenSindErreichbar() {
        for (Category category : Category.values()) {
            List<Document> hits = search(KnowledgeFilter.forCustomerFacingSearch(category));

            assertThat(hits)
                    .as("Kategorie %s", category)
                    .allSatisfy(hit -> assertThat(hit.getMetadata()).containsEntry(
                            KnowledgeMetadata.AUDIENCE, KnowledgeMetadata.AUDIENCE_CUSTOMER));
        }
    }

    /**
     * Haelt den Spring-AI-Fallstrick fest: ein geklammerter Ausdruck wird still ignoriert.
     * Schlaegt dieser Test irgendwann fehl, ist der Fehler in Spring AI behoben - dann darf
     * {@link KnowledgeFilter} auf die lesbarere Klammerform zurueck.
     */
    @Test
    void geklammerterAusdruckWirdVonSpringAiStillIgnoriert() {
        String geklammert =
                "(category == 'VERTRAGSFRAGE' || category == 'ALLGEMEIN') && audience == 'CUSTOMER'";

        assertThat(search(geklammert))
                .as("Wenn hier nur noch erlaubte Kategorien stehen, ist der Bug gefixt")
                .extracting(hit -> hit.getMetadata().get(KnowledgeMetadata.CATEGORY))
                .contains("TECHNISCHES_PROBLEM");
    }
}
