# spring-mcp-demo

Demo zum Thema MCP (Model Context Protocol) mit Spring Boot + Spring AI 2.0 und
React-Frontend. Zwei fachliche Domänen (Buchhandlung, Kundenverwaltung), jede mit
eigener REST-API und eigenem MCP-Server, plus ein Orchestrator-Backend, das per
`ChatClient` + MCP-Client mit OpenAI und beiden MCP-Servern spricht und dem Frontend
Chat- und Agenten-Endpunkte bereitstellt.

Details zu Architektur, Entscheidungen und Umsetzungsstand: [docs/MCP-Spring-Demo-Konzept.md](docs/MCP-Spring-Demo-Konzept.md).

> Kurzanleitung mit Screenshots (Chat, Agent, MCP Inspector): siehe [docs/02-anleitung.md](docs/02-anleitung.md).

## Architektur

```
React-Frontend (Vite+TS, Port 5173)
   │  REST (Chat + Agent)
   ▼
mcp-orchestrator-backend (Port 8090)
   │ MCP (HTTP+SSE)              │ MCP (HTTP+SSE)
   ▼                             ▼
mcp-buchhandlung-mcp-server      mcp-kundenverwaltung-mcp-server
   (Port 8082)                       (Port 8084)
   │ REST                            │ REST
   ▼                                 ▼
mcp-buchhandlung-domain-api      mcp-kundenverwaltung-domain-api
   (Port 8081)                       (Port 8083)
   │ JPA                             │ JPA
   ▼                                 ▼
   H2 (buchhandlung)                H2 (kundenverwaltung)
```

## URL-Übersicht

| Service | Port | Anwendung | Swagger UI / MCP-Endpoint | H2-Console |
|---|---|---|---|---|
| Frontend (Vite Dev-Server) | 5173 | http://localhost:5173 | – | – |
| Orchestrator-Backend | 8090 | http://localhost:8090 | http://localhost:8090/swagger-ui/index.html | – |
| Buchhandlung – Domain-API | 8081 | http://localhost:8081/api/books | http://localhost:8081/swagger-ui/index.html | http://localhost:8081/h2-console |
| Buchhandlung – MCP-Server | 8082 | – | http://localhost:8082/mcp (Streamable HTTP) | – |
| Kundenverwaltung – Domain-API | 8083 | http://localhost:8083/api/customers | http://localhost:8083/swagger-ui/index.html | http://localhost:8083/h2-console |
| Kundenverwaltung – MCP-Server | 8084 | – | http://localhost:8084/mcp (Streamable HTTP) | – |

Die MCP-Server haben kein eigenes REST-API/Swagger-UI, da sie nur das MCP-Protokoll
(JSON-RPC über HTTP+SSE) unter `/mcp` anbieten.

**H2-Console-Login:** Auf der Login-Seite die JDBC-URL der jeweiligen In-Memory-DB
eintragen (sonst verbindet die Console versehentlich mit einer neuen, leeren DB) —
Buchhandlung: `jdbc:h2:mem:buchhandlung`, Kundenverwaltung: `jdbc:h2:mem:kundenverwaltung`,
User `sa`, Passwort leer.

## Voraussetzungen

- Java 21+, Maven 3.9+
- Node.js 22+ / npm 11+
- Ein `OPENAI_API_KEY` (z. B. als Env-Var in der IntelliJ-Run-Config von
  `OrchestratorApplication`) — ohne gültigen Key starten die Domänen-Services und das
  Frontend trotzdem, aber `/api/chat` und `/api/agent` liefern nur einen Auth-Fehler von
  OpenAI zurück.

## Start (Reihenfolge beachten)

Die vier Domänen-Services können in beliebiger Reihenfolge/parallel gestartet werden,
der Orchestrator braucht aber **beide MCP-Server bereits laufend** (er verbindet sich
eager beim Boot und startet sonst nicht).

```bash
cd backend/mcp-buchhandlung-domain-api      && mvn spring-boot:run   # Port 8081
cd backend/mcp-buchhandlung-mcp-server      && mvn spring-boot:run   # Port 8082
cd backend/mcp-kundenverwaltung-domain-api  && mvn spring-boot:run   # Port 8083
cd backend/mcp-kundenverwaltung-mcp-server  && mvn spring-boot:run   # Port 8084

# erst wenn die vier oben laufen:
cd backend/mcp-orchestrator-backend && mvn spring-boot:run           # Port 8090 (braucht OPENAI_API_KEY)

# Frontend, unabhaengig von der Backend-Startreihenfolge:
cd frontend && npm install && npm run dev                            # Port 5173
```

Alternativ über die vorbereiteten IntelliJ-Run-Configs (`BuchhandlungDomainApiApplication`,
`BuchhandlungMcpServerApplication`, `KundenverwaltungDomainApiApplication`,
`KundenverwaltungMcpServerApplication`, `OrchestratorApplication`).

## Nutzung

**Chat (Tool-Calling), einzelne Domäne:**

```bash
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Welche Buecher gibt es?"}'
```

**Agent (mehrschrittig, domänenübergreifend), mit sichtbaren Zwischenschritten:**

```bash
curl -X POST http://localhost:8090/api/agent \
  -H "Content-Type: application/json" \
  -d '{"task": "Wie viel Geld hat Kunde 1 insgesamt ausgegeben und welche Buecher waren das?"}'
```

Oder im Frontend unter http://localhost:5173 über die Sidebar-Navigation ("Chat"/"Agent").

Weitere Beispiel-Fragen: siehe [docs/MCP-Spring-Demo-Konzept.md](docs/MCP-Spring-Demo-Konzept.md#beispiel-fragen-erweitert).

## MCP Inspector (Tools manuell testen)

Fürs Swagger UI der REST-APIs siehe die [URL-Übersicht](#url-übersicht) oben. Für die
MCP-Server selbst gibt es kein Swagger-Äquivalent von Spring — stattdessen der
[MCP Inspector](https://github.com/modelcontextprotocol/inspector) (Anthropic, generisch
für jeden MCP-Server, nicht Spring-spezifisch): zeigt alle Tools mit ihrem generierten
JSON-Schema und lässt sie interaktiv per Formular aufrufen, ohne `curl` und
`Mcp-Session-Id` von Hand zu basteln.

**Starten:**

```bash
npx @modelcontextprotocol/inspector
```

Die Konsole gibt eine URL mit Token aus, z. B.
`http://localhost:6274?MCP_INSPECTOR_API_TOKEN=...` — diese im Browser öffnen (Token gilt
nur für diesen lokalen Inspector-Prozess).

**Mit einem mcp-server verbinden:** im Inspector-UI (nicht in der Browser-Adressleiste!)
unter "Add Server" folgendes eintragen:

| Feld | Wert |
|---|---|
| Transport Type | `Streamable HTTP` |
| URL (Buchhandlung) | `http://localhost:8082/mcp` |
| URL (Kundenverwaltung) | `http://localhost:8084/mcp` |

Danach "Connect" klicken. Beide Server lassen sich parallel anlegen, im Haupt-UI
(Tools/Resources/Log-Stream) ist aber immer nur die gerade ausgewählte Verbindung aktiv —
zum Wechseln oben die andere konfigurierte Verbindung auswählen.

**Stolperfallen:**

* `http://localhost:8082/mcp`/`8084/mcp` sind **keine** normalen Webseiten — ein direkter
  Aufruf im Browser liefert `400 Invalid Accept header` bzw. `Session ID required`. Die
  URL gehört ins Connect-Formular des Inspectors, nicht in die Adressleiste.
* Der Log-Stream im Inspector zeigt nur die eigene Session. Aufrufe, die über das
  React-Frontend laufen, gehen durch die **eigene** MCP-Client-Session des
  `mcp-orchestrator-backend` und tauchen dort **nicht** auf — die beiden Domänen-Tools
  siehst du dort nur, wenn du sie direkt im Inspector aufrufst.
