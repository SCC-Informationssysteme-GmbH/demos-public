# Spring AI Showcase

Mini-Projekt, das sechs typische Bereiche der LLM-Integration mit Spring Boot demonstriert —
jede Anforderung aus [`docs/01-ueberblick.md`](docs/01-ueberblick.md) als eigener Kanal
(`CH.01`–`CH.06`) mit eigenem Backend-Modul und eigener Frontend-Seite. Das Frontend-Konzept
("Signal-Konsole") ist in [`docs/02-frontend.md`](docs/02-frontend.md) beschrieben. Eine
bebilderte Anleitung zu allen sechs Kanälen gibt es in
[`docs/03-anleitung.md`](docs/03-anleitung.md).

## Architektur

| Kanal | Backend-Package    | Thema                                                    |
|-------|---------------------|-----------------------------------------------------------|
| CH.01 | `llmrest`           | LLM per REST (OpenAI Chat Completions, WebClient)         |
| CH.02 | `prompt`            | Prompt-Orchestrierung / Templates (Zusammenfassung, Übersetzung, Sentiment) |
| CH.03 | `rag`               | RAG: Embeddings + In-Memory-Cosine-Similarity-Suche        |
| CH.04 | `vectordb`          | Vektordatenbank-Integration (Qdrant, persistent)           |
| CH.05 | `langchain`         | LangChain4J `AiServices` mit Session-Chat-Memory            |
| CH.06 | `businesslogic`     | KI-gestützte Business-Logik (Ticket-Klassifikation, JSON-Mode) |

- **Backend**: `backend/` — Java 21, Spring Boot 3.3.4, Maven
- **Frontend**: `frontend/` — React + Vite + TypeScript, Vite-Dev-Proxy leitet `/api` ans Backend weiter
- **Vektordatenbank**: Qdrant, lokal über Docker Compose

## OpenAI API: Chat vs. Embeddings — Unterschiede für CH.01–CH.06

Ein `OPENAI_API_KEY` mit Guthaben deckt beide Endpunkte ab. Der Unterschied liegt darin, **was** die Kanäle damit machen.

### Chat API
(z. B. `gpt-4o-mini` über `/chat/completions`, bei CH.05 via LangChain4j statt direktem WebClient-Call)

- Erzeugt Text als Antwort auf einen Prompt — klassisches "Frage → Antwort"-Verhalten.
- Wird gebraucht, wenn ein Kanal Dialoge führt, Texte zusammenfasst/übersetzt/klassifiziert oder Fragen beantwortet.
- Abrechnung nach Input- und Output-Tokens — pro Aufruf meist deutlich teurer als Embeddings.

### Embeddings API
(`text-embedding-3-small`)

- Wandelt Text in einen Vektor (eine Liste von Zahlen) um, der die Bedeutung des Texts repräsentiert.
- Erzeugt **keinen** Antworttext, sondern nur Zahlen — genutzt für semantische Suche/Ähnlichkeitsvergleiche (RAG, Vektorsuche).
- Günstiger pro Aufruf, aber oft viele Aufrufe nötig (z. B. ein Aufruf pro Dokument).

### Zuordnung der Kanäle zu Chat/Embeddings

| Kanal | Braucht | Warum (konkrete Klasse) |
|-------|---|---|
| CH.01 | nur Chat | `OpenAiChatService.complete()` — reiner Prompt-Aufruf, keine Suche beteiligt |
| CH.02 | nur Chat | nutzt dieselbe `OpenAiChatService` wie CH.01 (Template gerendert, dann Chat-Call) |
| CH.03 | Chat **+** Embeddings | `EmbeddingService` embedded Dokumente + Frage für die In-Memory-Suche (`RagVectorIndex`), `OpenAiChatService` generiert danach die Antwort aus dem gefundenen Kontext (`RagService`) |
| CH.04 | nur Embeddings | `QdrantVectorStoreService` nutzt dieselbe `EmbeddingService` wie CH.03 zum Indexieren/Suchen — es gibt dort keinen Chat-Call, `/search` liefert nur Treffer + Score, keine generierte Antwort |
| CH.05 | nur Chat | `LangChainConfig` baut einen LangChain4j-`ChatModel` (`OpenAiChatModel`) — kein Embeddings-Anteil |
| CH.06 | nur Chat | `TicketClassificationService` ruft `/chat/completions` mit `response_format: json_object` auf — kein Embeddings-Anteil |

### Wichtig zum Key

- Chat- und Embeddings-Endpunkte laufen hier über **denselben** Account/Key — es gibt keine getrennten Keys dafür.
- Wichtig ist nur genug Guthaben/Kontingent, da Chat-Aufrufe pro Anfrage teurer sind als Embeddings-Aufrufe.

## Voraussetzungen

- Java 21 (JDK)
- Maven (`mvn` muss im PATH verfügbar sein — kein Wrapper im Projekt enthalten)
- Node.js (für das Frontend)
- Docker Desktop (für Qdrant, nur für CH.04 nötig)
- Ein OpenAI-API-Key (`OPENAI_API_KEY`) mit Guthaben — für CH.01, CH.02, CH.03 (Chat + Embeddings), CH.04 (nur Embeddings), CH.05, CH.06

## Setup & Start

### 1. Umgebungsvariable setzen

```powershell
$env:OPENAI_API_KEY = "sk-..."
```

### 2. Qdrant starten (für CH.04)

```powershell
docker compose up -d
```

Startet einen Qdrant-Container (`ai-showcase-qdrant`) mit persistentem Volume, erreichbar unter
`http://localhost:6333`. Stoppen mit `docker compose down`.

### 3. Backend starten

```powershell
cd backend
mvn spring-boot:run
```

Läuft auf **Port 8100** (nicht 8080 — kollidiert sonst häufig mit anderen lokalen Diensten,
siehe `application.yml`). Swagger-UI: http://localhost:8100/swagger-ui/index.html

Zum manuellen Testen der Endpunkte ohne Swagger: `backend/requests.http` (IntelliJ HTTP Client)
enthält Beispiel-Requests für alle 6 Kanäle.

### 4. Frontend starten

```powershell
cd frontend
npm install
npm run dev
```

Der Vite-Dev-Server proxyt `/api`-Requests an `http://localhost:8100` (siehe `vite.config.ts`).

## Endpunkte je Kanal

| Kanal | Methode/Pfad                    | Beschreibung                                              |
|-------|----------------------------------|-------------------------------------------------------------|
| CH.01 | `POST /api/ch01/chat`           | `{prompt}` → Antwort vom LLM                                 |
| CH.02 | `GET /api/ch02/templates`       | Verfügbare Prompt-Templates auflisten                        |
| CH.02 | `POST /api/ch02/chat`           | `{templateKey, input}` → gerenderter Prompt + Antwort         |
| CH.03 | `GET /api/ch03/documents`       | Wissensbasis (HR-FAQ-Dokumente) auflisten                     |
| CH.03 | `POST /api/ch03/ask`            | `{question}` → Quellen + kontextbasierte Antwort              |
| CH.04 | `POST /api/ch04/index`          | Wissensbasis in Qdrant indexieren                             |
| CH.04 | `POST /api/ch04/search`         | `{query}` → Treffer aus Qdrant mit Score                       |
| CH.04 | `POST /api/ch04/documents`      | `{id, title, content}` → einzelnes Dokument direkt in Qdrant indexieren |
| CH.05 | `POST /api/ch05/chat`           | `{sessionId, message}` → Antwort, Verlauf bleibt pro Session erhalten |
| CH.05 | `DELETE /api/ch05/chat/{sessionId}` | Gesprächsverlauf der Session zurücksetzen                  |
| CH.06 | `POST /api/ch06/classify`       | `{ticketText}` → Kategorie, Priorität, Zusammenfassung, Antwortentwurf |

Jeder Kanal hat zusätzlich `GET /api/chXX/status` für einen einfachen Erreichbarkeits-Check.

## Eigene Dokumente für CH.03/CH.04

Die Wissensbasis wird zur Laufzeit aus `backend/src/main/resources/documents/*.md` geladen
(`RagKnowledgeBase`) — dieselben Dokumente nutzt auch CH.04 (Qdrant) über `/api/ch04/index`.
Eigene Dokumente einbinden: weitere `.md`-Dateien in diesen Ordner legen und die App neu starten.

Format je Datei:
- erste Zeile: `# Titel`
- restlicher Inhalt: der Dokumenttext
- Dateiname (ohne `.md`) wird zur Dokument-ID

## Konfiguration

Zentrale Einstellungen in `backend/src/main/resources/application.yml`:

| Key                       | Bedeutung                                          | Default                        |
|---------------------------|-----------------------------------------------------|---------------------------------|
| `server.port`             | Backend-Port                                        | `8100`                          |
| `openai.api-key`          | aus `OPENAI_API_KEY`-Env-Var                        | leer                            |
| `openai.model`            | Chat-Modell                                         | `gpt-4o-mini`                   |
| `openai.embedding-model`  | Embedding-Modell (CH.03/CH.04)                       | `text-embedding-3-small`        |
| `qdrant.base-url`         | aus `QDRANT_URL`-Env-Var                            | `http://localhost:6333`         |
| `qdrant.collection`       | Qdrant-Collection-Name                              | `ai-showcase-docs`              |

## Bekannte Stolpersteine

- **Port 8100 bereits belegt**: alten Backend-Prozess beenden, bevor neu gestartet wird.
- **CH.04 schlägt fehl**: Qdrant läuft nicht — `docker compose up -d` im Projekt-Root prüfen.
- **429 Too Many Requests von OpenAI**: Guthaben/Rate-Limit des hinterlegten API-Keys prüfen
  unter [platform.openai.com/usage](https://platform.openai.com/usage) — kein Bug im Code.
- **Doppelte/veraltete Treffer bei CH.04-Suche**: `/api/ch04/index` upsertet nur, alte Punkte mit
  inzwischen umbenannten/gelöschten Dokument-IDs bleiben in Qdrant erhalten. Bei Bedarf Collection
  droppen (`curl -X DELETE http://localhost:6333/collections/ai-showcase-docs`) und neu indexieren.
