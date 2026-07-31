package com.example.aishowcase.rag;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Lädt die Wissensbasis aus {@code classpath:documents/*.md}. Jede Datei beginnt mit einer
 * "# Titel"-Zeile, der Rest ist der Dokumentinhalt; der Dateiname (ohne Endung) wird zur ID.
 * Eigene Dokumente einbinden: einfach weitere .md-Dateien in diesen Ordner legen und die
 * Anwendung neu starten.
 */
@Component
public class RagKnowledgeBase {

    private static final String DOCUMENTS_LOCATION = "classpath:documents/*.md";

    private final List<RagDocument> documents;

    public RagKnowledgeBase(ResourcePatternResolver resourcePatternResolver) throws IOException {
        Resource[] resources = resourcePatternResolver.getResources(DOCUMENTS_LOCATION);
        this.documents = Arrays.stream(resources)
                .sorted(Comparator.comparing(resource -> filenameOf(resource)))
                .map(this::readDocument)
                .toList();
    }

    public List<RagDocument> all() {
        return documents;
    }

    private RagDocument readDocument(Resource resource) {
        String filename = filenameOf(resource);
        try (var reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine();
            if (firstLine == null || !firstLine.startsWith("# ")) {
                throw new IllegalStateException("Dokument " + filename + " muss mit einer '# Titel'-Zeile beginnen");
            }
            String title = firstLine.substring(2).trim();
            String content = reader.lines().collect(Collectors.joining("\n")).trim();
            return new RagDocument(withoutExtension(filename), title, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Dokument " + filename + " konnte nicht gelesen werden", e);
        }
    }

    private String filenameOf(Resource resource) {
        return Objects.requireNonNull(resource.getFilename(), "Ressource ohne Dateinamen: " + resource);
    }

    private String withoutExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
    }
}
