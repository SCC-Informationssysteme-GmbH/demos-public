package com.example.aiagents.knowledge;

/**
 * Metadaten-Vertrag zwischen Ingest (KnowledgeBaseLoader) und Abfrage (ResearchAgent).
 * Beide Seiten muessen sich auf dieselben Schluessel einigen, sonst filtert die
 * Vektorsuche ins Leere - ohne Fehler, nur ohne Treffer.
 */
public final class KnowledgeMetadata {

    /** Dateiname der Wissensquelle - Basis fuer den idempotenten Ingest. */
    public static final String SOURCE = "source";

    /** Fachkategorie der Wissensquelle, passend zu {@code com.example.aiagents.agent.Category}. */
    public static final String CATEGORY = "category";

    /** Anzeigetitel fuer die Quellenangabe im Antwortentwurf. */
    public static final String TITLE = "title";

    /** Kategorie fuer Quellen, die zu jeder Anfrage passen (Servicezeiten, Kontaktwege). */
    public static final String GENERAL_CATEGORY = "ALLGEMEIN";

    /** Sichtbarkeit der Quelle: darf daraus eine Kundenantwort entstehen? */
    public static final String AUDIENCE = "audience";

    /** Freigegeben fuer Kundenantworten. */
    public static final String AUDIENCE_CUSTOMER = "CUSTOMER";

    /**
     * Nur fuer interne Verwendung. Bewusst der Default: eine ohne Front Matter
     * abgelegte Datei (interne Anweisung, Protokoll, Vertragsentwurf) ist damit
     * automatisch von Kundenantworten ausgeschlossen. Der unsichere Fall muss
     * explizit freigegeben werden, nicht umgekehrt.
     */
    public static final String AUDIENCE_INTERNAL = "INTERNAL";

    private KnowledgeMetadata() {
    }
}
