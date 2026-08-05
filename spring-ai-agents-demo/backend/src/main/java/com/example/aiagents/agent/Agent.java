package com.example.aiagents.agent;

/**
 * Gemeinsames Interface aller Agenten (siehe docs/01, Abschnitt 1).
 * Ein Agent ist eine Spring-Service-Klasse: Eingabe-DTO rein, festes Ausgabe-DTO raus,
 * dazwischen genau ein LLM-Call mit festem System-Prompt.
 */
public interface Agent<I, O> {

    O process(I input);
}
