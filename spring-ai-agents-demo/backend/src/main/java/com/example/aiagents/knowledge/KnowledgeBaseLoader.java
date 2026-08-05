package com.example.aiagents.knowledge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Einmaliger Ingest beim Start, damit die Demo ohne manuellen Schritt sofort
 * benutzbar ist. Die eigentliche Arbeit liegt im {@link KnowledgeIngestService} und
 * ist ueber {@code POST /api/knowledge/reindex} jederzeit erneut aufrufbar - fuer
 * Aenderungen an den Wissensquellen ist also kein Neustart notwendig.
 *
 * <p>Abschaltbar mit {@code app.knowledge.index-on-startup=false}, etwa wenn der
 * Ingest von einem separaten Job oder nur einer Instanz uebernommen wird.
 */
@Component
public class KnowledgeBaseLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseLoader.class);

    private final KnowledgeIngestService ingestService;
    private final boolean indexOnStartup;

    public KnowledgeBaseLoader(KnowledgeIngestService ingestService,
                               @Value("${app.knowledge.index-on-startup}") boolean indexOnStartup) {
        this.ingestService = ingestService;
        this.indexOnStartup = indexOnStartup;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!indexOnStartup) {
            log.info("Ingest beim Start deaktiviert - POST /api/knowledge/reindex nutzen");
            return;
        }
        ingestService.reindex();
    }
}
