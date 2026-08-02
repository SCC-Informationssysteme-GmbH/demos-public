package de.scc.demo.freecad.service;

import de.scc.demo.freecad.config.AppProperties;
import de.scc.demo.freecad.exception.GenerationException;
import de.scc.demo.freecad.exception.GenerationException.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Ruft die OpenAI Chat-Completions-API auf und liefert den generierten
 * FreeCAD-Python-Code als reinen Text zurueck (siehe Anforderung 4.3).
 */
public class OpenAiCodeGenerator implements LlmCodeGenerator {

	private static final Logger log = LoggerFactory.getLogger(OpenAiCodeGenerator.class);

	private static final String SYSTEM_PROMPT = """
			Du generierst ausschliesslich FreeCAD-Python-API-Code (Module: FreeCAD, Part, Draft, Mesh; zusaetzlich
			das Python-Standardmodul "math" fuer Winkelberechnungen, falls noetig).
			Regeln:
			- Erzeuge ein Dokumentobjekt (z. B. per doc.addObject("Part::Feature", name) mit zugewiesener .Shape)
			  und exportiere es am Ende exakt so: Part.export([obj], "/work/output.stl")
			- Verwende ausschliesslich tatsaechlich existierende Funktionen der genannten Module. Erfinde keine
			  Attribute oder Methoden (z. B. existieren FreeCAD.cos/FreeCAD.sin NICHT - nutze dafuer math.cos/math.sin
			  mit Radiant-Werten).
			- Kein Dateisystem-Zugriff ausserhalb von /work.
			- Keine Netzwerkzugriffe, keine Shell-Aufrufe, keine Imports ausserhalb der genannten Module.
			- Gib ausschliesslich reinen Python-Code zurueck, ohne Markdown-Codeblock-Umrandung und ohne Erklaertext.
			""";

	private final RestClient restClient;
	private final String model;

	public OpenAiCodeGenerator(AppProperties.Llm.OpenAi config) {
		if (config.apiKey() == null || config.apiKey().isBlank()) {
			throw new IllegalStateException("OPENAI_API_KEY ist nicht gesetzt");
		}
		this.model = config.model();
		this.restClient = RestClient.builder()
				.baseUrl(config.baseUrl())
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.apiKey())
				.build();
	}

	@Override
	public String generateFreecadScript(String prompt) {
		ChatRequest request = new ChatRequest(model, List.of(
				new Message("system", SYSTEM_PROMPT),
				new Message("user", prompt)), 0.2);

		ChatResponse response;
		try {
			response = restClient.post()
					.uri("/chat/completions")
					.body(request)
					.retrieve()
					.body(ChatResponse.class);
		} catch (RestClientException ex) {
			log.error("OpenAI-Aufruf fehlgeschlagen", ex);
			throw new GenerationException(ErrorType.LLM_ERROR, "LLM-Anfrage fehlgeschlagen: " + ex.getMessage(), ex);
		}

		if (response == null || response.choices() == null || response.choices().isEmpty()) {
			throw new GenerationException(ErrorType.LLM_ERROR, "LLM hat keine Antwort geliefert");
		}

		String content = response.choices().get(0).message().content();
		if (content == null || content.isBlank()) {
			throw new GenerationException(ErrorType.LLM_ERROR, "LLM hat leeren Code geliefert");
		}
		return stripMarkdownFence(content.trim());
	}

	private String stripMarkdownFence(String code) {
		if (code.startsWith("```")) {
			int firstNewline = code.indexOf('\n');
			int lastFence = code.lastIndexOf("```");
			if (firstNewline > 0 && lastFence > firstNewline) {
				return code.substring(firstNewline + 1, lastFence).trim();
			}
		}
		return code;
	}

	private record ChatRequest(String model, List<Message> messages, double temperature) {
	}

	private record Message(String role, String content) {
	}

	private record ChatResponse(List<Choice> choices) {
	}

	private record Choice(Message message) {
	}
}
