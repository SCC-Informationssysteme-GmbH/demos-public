package com.example.aiagents.knowledge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Indexierung der Wissensquellen - aufrufbar zur Laufzeit, nicht an den
 * Applikationsstart gebunden.
 *
 * <p>Der Lauf ist <b>idempotent</b>, weil Qdrant persistiert:
 * <ul>
 *   <li>Point-ID = deterministische UUID aus Dateiname + Position + Inhalt, ein erneuter
 *       Lauf mit unveraendertem Inhalt ist damit ein Upsert auf dieselbe ID.</li>
 *   <li>Vor dem Schreiben werden die bestehenden Chunks der Datei geloescht - sonst
 *       blieben nach einer Kuerzung veraltete Chunks als Geister in der Suche.</li>
 * </ul>
 * Der Endpoint darf deshalb beliebig oft aufgerufen werden.
 */
@Service
public class KnowledgeIngestService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeIngestService.class);

    private final VectorStore vectorStore;
    private final List<String> locations;

    public KnowledgeIngestService(VectorStore vectorStore,
                                  @Value("${app.knowledge.location}") List<String> locations) {
        this.vectorStore = vectorStore;
        this.locations = locations;
    }

    public IngestResult reindex() throws IOException {
        Map<String, Resource> resources = resolve();
        if (resources.isEmpty()) {
            log.warn("Keine Wissensquellen unter {} gefunden", locations);
            return new IngestResult(locations, 0, 0, List.of());
        }

        List<IngestResult.IngestedSource> indexed = new ArrayList<>();
        int chunks = 0;
        for (Resource resource : resources.values()) {
            IngestResult.IngestedSource source = index(resource);
            indexed.add(source);
            chunks += source.chunks();
        }
        indexed.sort(Comparator.comparing(IngestResult.IngestedSource::source));
        removeOrphans(resources.keySet());

        log.info("Wissensquellen indexiert: {} Dateien, {} Chunks aus {}",
                resources.size(), chunks, locations);
        return new IngestResult(locations, resources.size(), chunks, indexed);
    }

    /**
     * Nimmt die <b>erste Location, die ueberhaupt etwas liefert</b>, und ignoriert die
     * uebrigen vollstaendig.
     *
     * <p>Der Grund fuer mehrere Locations: {@code classpath:} liefert die Kopie unter
     * {@code target/classes}, die nach einer Editor-Aenderung veraltet ist, solange nicht
     * neu gebaut wurde. Steht das Quellverzeichnis per {@code file:} davor, liest der
     * Reindex die aktuelle Fassung - ohne Build und ohne Neustart. Aus einem Jar heraus
     * greift der {@code file:}-Eintrag ins Leere und {@code classpath:} uebernimmt.
     *
     * <p>Bewusst <b>keine</b> Verschmelzung pro Dateiname: sonst wuerde eine aus dem
     * Quellbaum geloeschte Datei aus dem alten Build-Output wieder auftauchen und weiter
     * indexiert werden. Genau eine Location ist die Wahrheit.
     */
    private Map<String, Resource> resolve() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        for (String location : locations) {
            String pattern = location.trim();
            if (pattern.isEmpty()) {
                continue;
            }
            Map<String, Resource> byFilename = new LinkedHashMap<>();
            for (Resource resource : resolver.getResources(pattern)) {
                if (resource.isReadable() && resource.getFilename() != null) {
                    byFilename.putIfAbsent(resource.getFilename(), resource);
                }
            }
            if (!byFilename.isEmpty()) {
                log.debug("Wissensquellen aus Location {}", pattern);
                return byFilename;
            }
        }
        return Map.of();
    }

    /**
     * Entfernt Chunks von Quellen, die es nicht mehr gibt.
     *
     * <p>Ohne diesen Schritt ist der Ingest nur teil-idempotent: das Loeschen pro Datei
     * laeuft ausschliesslich fuer noch vorhandene Dateien. Eine aus der Wissensbasis
     * entfernte Quelle bliebe damit dauerhaft durchsuchbar - im Fall eines internen
     * Dokuments ein echtes Problem.
     */
    private void removeOrphans(Set<String> knownSources) {
        if (knownSources.stream().anyMatch(name -> name.contains("'"))) {
            log.warn("Dateiname mit Apostroph gefunden - Orphan-Bereinigung uebersprungen");
            return;
        }
        String list = knownSources.stream()
                .map("'%s'"::formatted)
                .collect(Collectors.joining(", ", "[", "]"));
        vectorStore.delete("%s nin %s".formatted(KnowledgeMetadata.SOURCE, list));
    }

    private IngestResult.IngestedSource index(Resource resource) throws IOException {
        String filename = resource.getFilename();
        FrontMatter frontMatter = FrontMatter.parse(resource.getContentAsString(StandardCharsets.UTF_8));
        String title = frontMatter.title() != null ? frontMatter.title() : filename;

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(KnowledgeMetadata.SOURCE, filename);
        metadata.put(KnowledgeMetadata.CATEGORY, frontMatter.category());
        metadata.put(KnowledgeMetadata.AUDIENCE, frontMatter.audience());
        metadata.put(KnowledgeMetadata.TITLE, title);

        List<Document> chunks = new TokenTextSplitter()
                .apply(List.of(new Document(frontMatter.body(), metadata)));

        // Erst aufraeumen, dann schreiben - macht den Lauf wiederholbar.
        vectorStore.delete("%s == '%s'".formatted(KnowledgeMetadata.SOURCE, filename));

        List<Document> points = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            Document chunk = chunks.get(i);
            points.add(new Document(pointId(filename, i, chunk.getText()),
                    chunk.getText(), chunk.getMetadata()));
        }
        vectorStore.add(points);

        String origin = resource.getURI().toString();
        log.debug("{} -> {} Chunks (category={}, audience={}) aus {}",
                filename, points.size(), frontMatter.category(), frontMatter.audience(), origin);
        return new IngestResult.IngestedSource(filename, frontMatter.category(),
                frontMatter.audience(), title, points.size(), origin);
    }

    /**
     * Qdrant-Point-IDs muessen UUIDs sein. Die UUID wird aus Datei, Position und Inhalt
     * abgeleitet, damit derselbe Inhalt immer dieselbe ID ergibt.
     */
    private String pointId(String filename, int index, String content) {
        String seed = filename + "#" + index + "#" + content;
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
