# MCP mit Spring

Stand: 07.08.2026 · Autor: Entwurf zur Abstimmung im Team · Status: Vertikaler Durchstich fertig und mit echtem OpenAI-Key end-to-end verifiziert, inkl. domänenübergreifender Agenten-Fragen

## Ziel

Beispiel zum Thema MCP (Model Context Protocol) mit Spring Boot und React-Frontend.
Zwei fachliche Domänen, jeweils mit eigener Demo-API und eigenem MCP-Server, der diese
API als Tools nach außen anbietet:

* Buchhandlung-Domäne
* Kundenverwaltung-Domäne

Die APIs sollen u. a. folgende Fragen beantworten können:

* Buchhandlung-Domäne:
  * Welche Bücher gibt es?
  * Welche Bücher hat der Kunde XY bestellt?
* Kundenverwaltung-Domäne:
  * Welche Kunden gibt es?
* Kombiniert (Kunde):
  * Wie viele Bücher habe ich in einem Zeitraum gekauft?
  * Wie viel Geld habe ich in einem Zeitraum ausgegeben?

Siehe [Beispiel-Fragen](#beispiel-fragen-erweitert) für eine erweiterte Liste, inklusive
domänenübergreifender Fragen, die den Mehrwert von MCP + Agenten-Modus zeigen.

## Umsetzungsstand

Alle Schritte aus [Reihenfolge der Umsetzung](#reihenfolge-der-umsetzung) sind fertig und
end-to-end getestet, inklusive echtem OpenAI-Key:

* **Buchhandlung** (`mcp-buchhandlung-common`/`-domain-api`/`-mcp-server`): H2 + JPA,
  4 Beispielbücher, 4 Bestellungen. Tools: `list_books`, `get_book`,
  `list_orders_for_customer`.
* **Kundenverwaltung** (`mcp-kundenverwaltung-common`/`-domain-api`/`-mcp-server`): H2 + JPA,
  3 Beispielkunden (IDs 1/2 passen zu den Buchhandlung-Bestellungen). Tools: `list_customers`,
  `get_customer`.
* **`mcp-orchestrator-backend`**: verbindet sich beim Start per MCP (Streamable HTTP) mit
  beiden mcp-servern, bietet `POST /api/chat` (Tool-Calling-Chat) und `POST /api/agent`
  (Agenten-Modus, gibt zusätzlich die protokollierten Zwischenschritte `tool`/`input`/`output`
  zurück).
* **Frontend** (Vite + React + TS): Chat- und Agent-Tab gegen den Orchestrator, Dev-Proxy
  `/api` → Port 8090.
* Reactor-Parent wurde von `mcp-buchhandlung-parent` in `mcp-demo-parent` umbenannt, da er
  jetzt alle Module (beide Domänen + Orchestrator) trägt.
* **Swagger UI** (`springdoc-openapi-starter-webmvc-ui`) für alle drei REST-APIs unter
  `/swagger-ui/index.html` (OpenAPI-JSON unter `/v3/api-docs`): Buchhandlung-domain-api
  (Port 8081), Kundenverwaltung-domain-api (Port 8083), Orchestrator (Port 8090). Die
  mcp-server-Module haben keine eigene Swagger UI, da sie nur das MCP-Protokoll (kein
  klassisches REST-API) unter `/mcp` anbieten.

**Verifiziert:** komplette Kette (beide domain-apis + beide mcp-server + Orchestrator)
gemeinsam mit echtem `OPENAI_API_KEY` (aus der IntelliJ-Run-Config von
`OrchestratorApplication`) gestartet:

* `POST /api/chat` mit "Welche Buecher gibt es?" und "Welche Kunden gibt es?" liefert
  jeweils die korrekten Seed-Daten als natürlichsprachige Antwort (Tool-Calling-Chat
  funktioniert für beide Domänen).
* `POST /api/agent` mit der domänenübergreifenden Frage "Wie viel Geld hat Kunde 1
  insgesamt ausgegeben und welche Buecher waren das?" hat selbstständig 4 Tool-Aufrufe über
  **beide** Domänen kombiniert (`list_orders_for_customer` → `get_customer` →
  `get_book` ×2) und alle Zwischenschritte korrekt protokolliert; die Endsumme (134,00 €)
  war rechnerisch richtig — genau das Szenario, das den Mehrwert von MCP + Agenten-Modus
  zeigen sollte.
* Vorher schon mit Dummy-Key bestätigt: Orchestrator verbindet sich beim Boot zu beiden
  Servern (`Implementation[name=...]` im Log), Anfragen erreichen nachweislich die
  OpenAI-API (`401 Incorrect API key provided` bei ungültigem Key).

**Bekannter Stolperstein:** `spring.ai.mcp.server.protocol` hat einen POJO-Default
(`streamable`), der von der internen `@ConditionalOnProperty`-Prüfung aber nicht gesehen
wird — ohne explizites `spring.ai.mcp.server.protocol: STREAMABLE` in der
`application.yml` bleibt der `/mcp`-Endpoint unregistriert (404). Ist in beiden mcp-servern
gesetzt.

## Architektur-Übersicht

```
React-Frontend (Vite+TS)
   │  REST (Chat + Agent)
   ▼
mcp-orchestrator-backend  (hält OPENAI_API_KEY, Spring AI ChatClient, MCP-Client für beide Domänen)
   │ MCP (HTTP+SSE)              │ MCP (HTTP+SSE)
   ▼                             ▼
mcp-buchhandlung-mcp-server      mcp-kundenverwaltung-mcp-server
   │ REST                            │ REST
   ▼                                 ▼
mcp-buchhandlung-domain-api      mcp-kundenverwaltung-domain-api
   │ JPA                             │ JPA
   ▼                                 ▼
   H2 (buchhandlung)                H2 (kundenverwaltung)
```

Der Orchestrator ist eine bewusste Erweiterung gegenüber dem ersten Entwurf: React kann
den OpenAI-Key nicht sicher halten und sollte kein MCP-Client sein. Ohne einen eigenen
Orchestrator müsste einer der fachlichen MCP-Server diese Rolle zusätzlich übernehmen,
was ihn architektonisch verunreinigen würde (er wäre dann nicht mehr rein fachlich).

## Technologieentscheidungen

| Thema | Entscheidung |
|---|---|
| MCP-Server-Implementierung | Spring AI MCP Server Starter (nicht selbst gebaut) |
| Datenhaltung | H2 + Spring Data JPA (nicht In-Memory-Listen) |
| LLM | OpenAI (Key über Env-Var `OPENAI_API_KEY`, gesetzt in der Run-Config des Orchestrators, analog zu spring-ai-showcase) |
| MCP-Transport | HTTP + SSE (Streamable HTTP) — passt zu eigenständig deploybaren Services, im Gegensatz zu STDIO |
| Reihenfolge Umsetzung | erst Buchhandlung komplett vertikal, danach Kundenverwaltung nach demselben Muster |
| Frontend-Scope v1 | Chat mit Tool-Calling **und** separater Agenten-Modus für mehrschrittige Aufgaben, von Anfang an |

## Multi-Modul-Projekt

Jede Domäne wird in Maven-Module aufgeteilt, sodass eigenständig deploybare
Spring-Boot-Anwendungen entstehen.

| Modul | Rolle | Port (Buchhandlung / Kundenverwaltung) | Eigenständig deploybar? |
|---|---|---|---|
| `mcp-<domaene>-common` | Geteilte DTOs (Vertrag zwischen domain-api und mcp-server) | – | Nein, reine Bibliothek |
| `mcp-<domaene>-domain-api` | Fach-API (Spring Boot + Spring Data JPA + H2) | 8081 / 8083 | Ja |
| `mcp-<domaene>-mcp-server` | Spring AI MCP Server, übersetzt Fach-API in MCP-Tools (HTTP+SSE) | 8082 / 8084 | Ja |

Zusätzlich:

| Modul | Rolle | Port |
|---|---|---|
| `mcp-orchestrator-backend` | Hält OpenAI-Key, Spring AI `ChatClient` + MCP-Client-Anbindung an beide mcp-server, bietet REST-API für Chat- und Agent-Modus | 8090 |
| `frontend` | React (Vite+TS), Chat-UI + Agent-UI, gegen `mcp-orchestrator-backend` per Dev-Proxy | 5173 (dev) |

## Reihenfolge der Umsetzung

1. ✅ **Buchhandlung vertikal**: `common` → `domain-api` (H2, JPA, Beispieldaten) → `mcp-server`
   (Spring AI MCP Server Starter, Tools für Bücher/Bestellungen) → manuell getestet (echter
   MCP-Handshake über curl)
2. ✅ **Orchestrator** ans Laufen gebracht, MCP-Client + `ChatClient`, Chat-Modus mit
   Tool-Calling
3. ✅ **Frontend** Chat-UI gegen Orchestrator
4. ✅ **Agenten-Modus** im Orchestrator + Frontend ergänzt (mehrschrittige Tool-Aufrufe mit
   sichtbaren Zwischenschritten über `RecordingToolCallback`)
5. ✅ **Kundenverwaltung** nach demselben Muster wie Buchhandlung ergänzt, Orchestrator um
   zweiten MCP-Client erweitert
6. ✅ Domänenübergreifende Fragen mit echtem `OPENAI_API_KEY` end-to-end getestet

## Beispiel-Fragen (erweitert)

Buchhandlung:
* Welche Bücher gibt es? / Welche Bücher zu Thema/Autor X?
* Welche Bücher hat Kunde XY bestellt?
* Wie viele Bücher wurden im Zeitraum X verkauft?

Kundenverwaltung:
* Welche Kunden gibt es? / Kundendetails zu XY?
* Wie viele Bücher hat Kunde XY in Zeitraum X gekauft?
* Wie viel Geld hat Kunde XY in Zeitraum X ausgegeben?

Domänenübergreifend (zeigt den Mehrwert von MCP + Agenten-Modus):
* Liste alle Kunden, die in Zeitraum X mehr als Betrag Y ausgegeben haben, inkl. ihrer gekauften Bücher
* Welches ist das meistverkaufte Buch unter Kunden aus Region/Segment Z?
* Empfiehl Kunde XY ein neues Buch basierend auf bisherigen Käufen

## Datenmodell (Stand nach Umsetzung)

* **Buch** (`BookDto`): `id`, `isbn`, `title`, `author`, `price`
* **Bestellung** (`OrderDto`): `id`, `customerId`, `bookId`, `quantity`, `orderDate`
* **Kunde** (`CustomerDto`): `id`, `name`, `email`, `city`

`customerId`/`bookId` sind bewusst einfache Long-Referenzen ohne DB-Fremdschlüssel über
Modulgrenzen hinweg (jede Domäne hat ihre eigene H2-Instanz) — die Verknüpfung passiert erst
zur Laufzeit über den Orchestrator/Agenten.

## Offene Punkte

* Security/Auth zwischen den Modulen — für Demo-Zweck zunächst offen/ungesichert, ggf.
  späterer Ausbauschritt
* Kundenverwaltung nutzt bewusst denselben Orchestrator wie Buchhandlung (zwei MCP-Clients
  in einem Backend) — kein eigener Orchestrator pro Domäne
* Frontend (Chat-/Agent-UI) wurde bisher nur strukturell getestet (Typecheck, Lint, Build,
  Dev-Proxy per curl) — noch nicht visuell im Browser mit echtem Key durchgeklickt
