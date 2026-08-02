# Spring AI Demo

Mini-Projekt, das sechs typische Bereiche der LLM-Integration mit Spring Boot demonstriert —
jede Anforderung aus [`docs/01-ueberblick.md`](docs/01-ueberblick.md) als eigener Kanal
(`CH.01`–`CH.06`) mit eigenem Backend-Modul und eigener Frontend-Seite. Das Frontend-Konzept
("Signal-Konsole") ist in [`docs/02-frontend.md`](docs/02-frontend.md) beschrieben. Eine
bebilderte Anleitung zu allen sechs Kanälen gibt es in
[`docs/03-anleitung.md`](docs/03-anleitung.md).

## Architektur

| Kanal | Backend-Package    | Thema                                                    |
|-------|---------------------|-----------------------------------------------------------|
| CH.01 | `llmrest`           | LLM per REST (Spring AI `ChatClient`, OpenAI-Modell)      |
| CH.02 | `prompt`            | Prompt-Orchestrierung / Templates (Spring AI `PromptTemplate`) |
| CH.03 | `rag`               | RAG: Spring AI `EmbeddingModel` + In-Memory-Cosine-Similarity-Suche |
| CH.04 | `vectordb`          | Vektordatenbank-Integration (Spring AI `VectorStore`, Qdrant-Backend, persistent) |
| CH.05 | `langchain`         | LangChain4J `AiServices` mit Session-Chat-Memory (bewusst *ohne* Spring AI, als Alternativen-Vergleich) |
| CH.06 | `businesslogic`     | KI-gestützte Business-Logik (Ticket-Klassifikation, strukturierte Ausgabe via `ChatClient.entity()`) |

- **Backend**: `backend/` — Java 21, Spring Boot 3.5.16, Spring AI 1.1.0, Maven
- **Frontend**: `frontend/` — React + Vite + TypeScript, Vite-Dev-Proxy leitet `/api` ans Backend weiter
- **Vektordatenbank**: Qdrant, lokal über Docker Compose

CH.01–CH.04 und CH.06 nutzen das Spring-AI-Framework (`org.springframework.ai`) als Abstraktion
über die OpenAI-API; CH.05 bleibt bewusst bei LangChain4j, um beide Ansätze im selben Projekt
gegenüberzustellen.

## OpenAI API: Chat vs. Embeddings — Unterschiede für CH.01–CH.06

Ein `OPENAI_API_KEY` mit Guthaben deckt beide Endpunkte ab. Der Unterschied liegt darin, **was** die Kanäle damit machen.

### Chat API
(z. B. `gpt-4o-mini`, bei CH.01–CH.04/CH.06 über Spring AIs `ChatClient`, bei CH.05 via LangChain4j)

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
| CH.01 | nur Chat | `OpenAiChatService.complete()` — Spring-AI-`ChatClient`-Aufruf, keine Suche beteiligt |
| CH.02 | nur Chat | nutzt dieselbe `OpenAiChatService` wie CH.01 (Spring-AI-`PromptTemplate` rendert, dann Chat-Call) |
| CH.03 | Chat **+** Embeddings | `EmbeddingService` (Spring AI `EmbeddingModel`) embedded Dokumente + Frage für die In-Memory-Suche (`RagVectorIndex`), `OpenAiChatService` generiert danach die Antwort aus dem gefundenen Kontext (`RagService`) |
| CH.04 | nur Embeddings | `QdrantVectorStoreService` nutzt die Spring-AI-`VectorStore`-Abstraktion (Qdrant-Backend) zum Indexieren/Suchen — es gibt dort keinen Chat-Call, `/search` liefert nur Treffer + Score, keine generierte Antwort |
| CH.05 | nur Chat | `LangChainConfig` baut einen LangChain4j-`ChatModel` (`OpenAiChatModel`) — kein Embeddings-Anteil, kein Spring AI |
| CH.06 | nur Chat | `TicketClassificationService` ruft `ChatClient.prompt().call().entity(TicketClassification.class)` auf — Spring AI erzeugt Format-Vorgaben und parst die Antwort automatisch, kein Embeddings-Anteil |

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

Startet einen Qdrant-Container (`ai-demo-qdrant`) mit persistentem Volume, erreichbar unter
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

| Key                                        | Bedeutung                                          | Default                        |
|--------------------------------------------|-----------------------------------------------------|---------------------------------|
| `server.port`                              | Backend-Port                                        | `8100`                          |
| `spring.ai.openai.api-key`                 | aus `OPENAI_API_KEY`-Env-Var (CH.01–CH.04/CH.06)     | leer                            |
| `spring.ai.openai.chat.options.model`      | Chat-Modell                                         | `gpt-4o-mini`                   |
| `spring.ai.openai.embedding.options.model` | Embedding-Modell (CH.03/CH.04)                       | `text-embedding-3-small`        |
| `spring.ai.vectorstore.qdrant.host`        | aus `QDRANT_HOST`-Env-Var                           | `localhost`                     |
| `spring.ai.vectorstore.qdrant.port`        | gRPC-Port, aus `QDRANT_GRPC_PORT`-Env-Var            | `6334`                          |
| `spring.ai.vectorstore.qdrant.collection-name` | Qdrant-Collection-Name                          | `ai-demo-docs`              |
| `openai.api-key`                           | aus `OPENAI_API_KEY`-Env-Var, **nur für CH.05** (LangChain4j, eigenstaendig von Spring AI) | leer |
| `openai.model`                             | Chat-Modell für CH.05                                | `gpt-4o-mini`                   |

## Bekannte Stolpersteine

- **Port 8100 bereits belegt**: alten Backend-Prozess beenden, bevor neu gestartet wird.
- **CH.04 schlägt fehl**: Qdrant läuft nicht — `docker compose up -d` im Projekt-Root prüfen.
- **429 Too Many Requests von OpenAI**: Guthaben/Rate-Limit des hinterlegten API-Keys prüfen
  unter [platform.openai.com/usage](https://platform.openai.com/usage) — kein Bug im Code.
- **Doppelte/veraltete Treffer bei CH.04-Suche**: `/api/ch04/index` upsertet nur, alte Punkte mit
  inzwischen umbenannten/gelöschten Dokument-IDs bleiben in Qdrant erhalten. Bei Bedarf Collection
  droppen (`curl -X DELETE http://localhost:6333/collections/ai-demo-docs`) und neu indexieren.
- **IntelliJ zeigt überall "Cannot resolve symbol" (z. B. `springframework`), obwohl `mvn compile`
  auf der Kommandozeile fehlerfrei läuft**: passiert nach einem Bump der Spring-Boot-/Spring-AI-Version
  in `pom.xml` — IntelliJs internes Projektmodell übernimmt die neuen Abhängigkeiten manchmal nicht,
  auch nicht nach "Reload Maven Project". Fix: **File → Invalidate Caches... → Invalidate and Restart**
  (Modul-Neuimport erzwingen).
