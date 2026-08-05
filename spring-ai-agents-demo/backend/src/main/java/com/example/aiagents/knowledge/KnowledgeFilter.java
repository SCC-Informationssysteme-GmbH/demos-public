package com.example.aiagents.knowledge;

import com.example.aiagents.agent.Category;

/**
 * Baut den Filter-Ausdruck fuer die Vektorsuche.
 *
 * <p><b>Achtung, verifizierte Fallstricke von Spring AI 1.1.0 mit Qdrant:</b> ein
 * Filter-Ausdruck mit <b>Klammer-Gruppierung</b> wird still ignoriert - die Suche liefert
 * dann alle Dokumente, ohne Fehler und ohne Warnung. Gemessen mit demselben Datenbestand:
 *
 * <pre>
 *   category == 'X'                                  -> wirkt
 *   category == 'X' || category == 'Y'               -> wirkt
 *   category == 'X' && audience == 'CUSTOMER'        -> wirkt
 *   category in ['X','Y'] && audience == 'CUSTOMER'  -> wirkt
 *   (category == 'X' || category == 'Y') && audience == 'CUSTOMER'  -> WIRKT NICHT
 *   audience == 'CUSTOMER' && (category == 'X' || category == 'Y')  -> WIRKT NICHT
 * </pre>
 *
 * Deshalb wird die Kategorie-Alternative als {@code in [...]} formuliert statt als
 * geklammertes ODER. Semantisch identisch, aber wirksam. Wer das hier auf Klammern
 * umschreibt, deaktiviert unbemerkt den Sichtbarkeitsschutz - der Test
 * {@code KnowledgeFilterTest} faengt das.
 */
public final class KnowledgeFilter {

    private KnowledgeFilter() {
    }

    /**
     * Beschraenkt die Suche auf Quellen der Kategorie plus die allgemein gueltigen - und
     * in jedem Fall auf solche, die fuer Kundenantworten freigegeben sind.
     *
     * <p>Die Kategorie ist bewusst eine Alternative: Servicezeiten und Kontaktwege passen
     * zu jeder Anfrage, und eine Fehlklassifizierung wuerde den Agenten bei einer harten
     * Einschraenkung vollstaendig blind machen. Die Sichtbarkeit ist dagegen eine
     * UND-Bedingung und nicht verhandelbar.
     */
    public static String forCustomerFacingSearch(Category category) {
        String categories = category == null
                ? "'%s'".formatted(KnowledgeMetadata.GENERAL_CATEGORY)
                : "'%s','%s'".formatted(category.name(), KnowledgeMetadata.GENERAL_CATEGORY);

        return "%s in [%s] && %s == '%s'".formatted(
                KnowledgeMetadata.CATEGORY, categories,
                KnowledgeMetadata.AUDIENCE, KnowledgeMetadata.AUDIENCE_CUSTOMER);
    }
}
