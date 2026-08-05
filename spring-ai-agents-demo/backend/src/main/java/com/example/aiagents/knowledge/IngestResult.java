package com.example.aiagents.knowledge;

import java.util.List;

/**
 * Ergebnis eines Ingest-Laufs - Rueckgabe des Reindex-Endpoints, damit sichtbar ist,
 * was tatsaechlich im Index gelandet ist (inklusive Sichtbarkeit und Herkunft).
 */
public record IngestResult(List<String> locations,
                           int files,
                           int chunks,
                           List<IngestedSource> sources) {

    /**
     * @param origin aufgeloester Pfad der Datei. Beantwortet die Frage, ob der Lauf die
     *               bearbeitete Datei im Quellbaum gelesen hat oder die aeltere Kopie
     *               unter {@code target/classes}.
     */
    public record IngestedSource(String source,
                                 String category,
                                 String audience,
                                 String title,
                                 int chunks,
                                 String origin) {
    }
}
