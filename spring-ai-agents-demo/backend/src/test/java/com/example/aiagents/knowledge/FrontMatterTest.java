package com.example.aiagents.knowledge;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kein Spring-Kontext notwendig - reine Parser-Logik.
 */
class FrontMatterTest {

    /**
     * Der wichtigste Test der Klasse: die Sichtbarkeit schuetzt davor, dass interne
     * Dokumente in Kundenantworten landen. Kippt der Default auf CUSTOMER, faellt es
     * nur hier auf - im Betrieb erst, wenn ein Entwurf interne Prozessdetails zitiert.
     */
    @Test
    void ohneFrontMatterIstEineQuelleInternUndDamitFuerKundenGesperrt() {
        FrontMatter parsed = FrontMatter.parse("# Interne Anweisung\n\nNur fuer Mitarbeitende.");

        assertThat(parsed.audience()).isEqualTo(KnowledgeMetadata.AUDIENCE_INTERNAL);
        assertThat(parsed.category()).isEqualTo(KnowledgeMetadata.GENERAL_CATEGORY);
        assertThat(parsed.title()).isNull();
        assertThat(parsed.body()).startsWith("# Interne Anweisung");
    }

    @Test
    void frontMatterSetztKategorieSichtbarkeitUndTitel() {
        FrontMatter parsed = FrontMatter.parse("""
                ---
                category: VERTRAGSFRAGE
                audience: customer
                title: Vertraege und Abrechnung
                ---

                # Vertraege und Abrechnung
                Inhalt.
                """);

        assertThat(parsed.category()).isEqualTo("VERTRAGSFRAGE");
        assertThat(parsed.audience()).isEqualTo(KnowledgeMetadata.AUDIENCE_CUSTOMER);
        assertThat(parsed.title()).isEqualTo("Vertraege und Abrechnung");
        assertThat(parsed.body()).startsWith("# Vertraege und Abrechnung");
    }

    @Test
    void unvollstaendigesFrontMatterFaelltAufDefaultsZurueck() {
        FrontMatter parsed = FrontMatter.parse("""
                ---
                title: Nur ein Titel
                ---

                Inhalt.
                """);

        assertThat(parsed.category()).isEqualTo(KnowledgeMetadata.GENERAL_CATEGORY);
        assertThat(parsed.audience()).isEqualTo(KnowledgeMetadata.AUDIENCE_INTERNAL);
        assertThat(parsed.title()).isEqualTo("Nur ein Titel");
    }

    /**
     * Ein nicht geschlossener Block darf nicht dazu fuehren, dass der Rest der Datei
     * als Header interpretiert und der Inhalt verworfen wird.
     */
    @Test
    void nichtGeschlossenesFrontMatterGiltAlsInhalt() {
        String raw = "---\ncategory: VERTRAGSFRAGE\n\n# Ueberschrift\nInhalt.";

        FrontMatter parsed = FrontMatter.parse(raw);

        assertThat(parsed.category()).isEqualTo(KnowledgeMetadata.GENERAL_CATEGORY);
        assertThat(parsed.audience()).isEqualTo(KnowledgeMetadata.AUDIENCE_INTERNAL);
        assertThat(parsed.body()).isEqualTo(raw);
    }
}
