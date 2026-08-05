package com.example.aiagents.api;

import com.example.aiagents.knowledge.IngestResult;
import com.example.aiagents.knowledge.KnowledgeIngestService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeIngestService ingestService;

    public KnowledgeController(KnowledgeIngestService ingestService) {
        this.ingestService = ingestService;
    }

    @Operation(summary = "Wissensquellen neu indexieren",
            description = """
                    Liest die Wissensquellen erneut ein und schreibt sie nach Qdrant.
                    Der Lauf ist idempotent und darf beliebig oft aufgerufen werden:
                    unveraenderte Chunks werden per Upsert ersetzt, entfallene entfernt.

                    Damit ist fuer Aenderungen an den Wissensquellen kein Neustart noetig.
                    Achtung bei der Standard-Location `classpath:`: eine im Editor
                    geaenderte Datei wirkt erst, wenn sie nach target/classes kopiert
                    wurde (IntelliJ: Build Project). Zeigt `app.knowledge.location` per
                    `file:`-Prefix direkt auf das Verzeichnis, entfaellt auch das.
                    """)
    @PostMapping("/reindex")
    public IngestResult reindex() throws IOException {
        return ingestService.reindex();
    }
}
