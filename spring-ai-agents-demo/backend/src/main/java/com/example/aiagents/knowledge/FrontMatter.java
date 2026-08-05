package com.example.aiagents.knowledge;

/**
 * Minimaler Parser fuer das YAML-artige Front Matter der Wissensquellen:
 *
 * <pre>
 * ---
 * category: VERTRAGSFRAGE
 * audience: CUSTOMER
 * title: Vertraege und Abrechnung
 * ---
 * </pre>
 *
 * Damit beschreibt sich eine neue Wissensquelle selbst - eine zusaetzliche Datei
 * erfordert keine Java-Aenderung. Die Defaults sind bewusst restriktiv: fehlt das
 * Front Matter, gilt Kategorie ALLGEMEIN und Sichtbarkeit INTERNAL. Eine versehentlich
 * abgelegte interne Datei wird damit nicht Teil von Kundenantworten.
 */
record FrontMatter(String category, String audience, String title, String body) {

    static final String DEFAULT_CATEGORY = KnowledgeMetadata.GENERAL_CATEGORY;
    static final String DEFAULT_AUDIENCE = KnowledgeMetadata.AUDIENCE_INTERNAL;
    private static final String DELIMITER = "---";

    static FrontMatter parse(String raw) {
        String normalized = raw.stripLeading();
        if (!normalized.startsWith(DELIMITER)) {
            return withDefaults(raw);
        }

        int end = normalized.indexOf(DELIMITER, DELIMITER.length());
        if (end < 0) {
            return withDefaults(raw);
        }

        String header = normalized.substring(DELIMITER.length(), end);
        String body = normalized.substring(end + DELIMITER.length()).stripLeading();

        String category = DEFAULT_CATEGORY;
        String audience = DEFAULT_AUDIENCE;
        String title = null;
        for (String line : header.split("\\R")) {
            int colon = line.indexOf(':');
            if (colon < 0) {
                continue;
            }
            String key = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            if (value.isEmpty()) {
                continue;
            }
            if ("category".equalsIgnoreCase(key)) {
                category = value;
            } else if ("audience".equalsIgnoreCase(key)) {
                audience = value.toUpperCase();
            } else if ("title".equalsIgnoreCase(key)) {
                title = value;
            }
        }
        return new FrontMatter(category, audience, title, body);
    }

    private static FrontMatter withDefaults(String raw) {
        return new FrontMatter(DEFAULT_CATEGORY, DEFAULT_AUDIENCE, null, raw);
    }
}
