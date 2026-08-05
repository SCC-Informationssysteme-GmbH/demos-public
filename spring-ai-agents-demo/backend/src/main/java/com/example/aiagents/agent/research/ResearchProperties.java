package com.example.aiagents.agent.research;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Stellschrauben des Retrieval. Die beiden Werte begrenzen Unterschiedliches:
 * {@code topK} die <b>Menge</b> der Treffer, {@code similarityThreshold} deren
 * <b>Qualitaet</b>.
 *
 * @param topK                maximale Anzahl Chunks, die in den Prompt gehen. Mehr erhoeht
 *                            die Trefferwahrscheinlichkeit, kostet aber Input-Tokens und
 *                            laesst den relevanten Chunk mit schwachen konkurrieren.
 * @param similarityThreshold Mindest-Aehnlichkeit von 0 bis 1. Bei 0 liefert die Suche
 *                            immer bis zu {@code topK} Treffer - auch inhaltlich
 *                            unpassende, einfach weil es keine besseren gibt. Ein
 *                            Schwellwert verwirft die.
 */
@ConfigurationProperties(prefix = "app.research")
public record ResearchProperties(int topK, double similarityThreshold) {

    public ResearchProperties {
        if (topK < 1) {
            throw new IllegalArgumentException(
                    "app.research.top-k muss mindestens 1 sein, war: " + topK);
        }
        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException(
                    "app.research.similarity-threshold muss zwischen 0 und 1 liegen, war: "
                            + similarityThreshold);
        }
    }
}
