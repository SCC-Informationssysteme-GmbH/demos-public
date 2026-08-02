package com.example.aidemo.prompt;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PromptTemplateService {

    private final Map<String, PromptTemplate> templates = new LinkedHashMap<>();

    public PromptTemplateService() {
        register(new PromptTemplate(
                "summary",
                "Zusammenfassung",
                "Du bist ein präziser Zusammenfassungs-Assistent.",
                "Fasse den folgenden Text in maximal drei Sätzen zusammen:\n\n{input}"
        ));
        register(new PromptTemplate(
                "translate-en",
                "Übersetzung (DE → EN)",
                "Du bist ein professioneller Übersetzer.",
                "Übersetze den folgenden Text ins Englische:\n\n{input}"
        ));
        register(new PromptTemplate(
                "classify-sentiment",
                "Sentiment-Klassifikation",
                "Du klassifizierst die Stimmung eines Textes. Antworte nur mit einem Wort: POSITIV, NEUTRAL oder NEGATIV.",
                "Text: {input}"
        ));
        register(new PromptTemplate(
                "email-reply",
                "E-Mail-Antwortentwurf",
                "Du bist ein professioneller, freundlicher Kundenservice-Mitarbeiter. "
                        + "Formuliere aus den gegebenen Stichpunkten eine vollständige, höfliche E-Mail-Antwort auf Deutsch.",
                "Stichpunkte für die Antwort:\n\n{input}"
        ));
        register(new PromptTemplate(
                "extract-bullets",
                "Stichpunkte extrahieren",
                "Du extrahierst die wichtigsten Punkte aus einem Text.",
                "Fasse den folgenden Text als nummerierte Liste der wichtigsten Punkte zusammen:\n\n{input}"
        ));
    }

    private void register(PromptTemplate template) {
        templates.put(template.key(), template);
    }

    public List<PromptTemplate> all() {
        return List.copyOf(templates.values());
    }

    public PromptTemplate get(String key) {
        PromptTemplate template = templates.get(key);
        if (template == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unbekanntes Template: " + key);
        }
        return template;
    }
}
