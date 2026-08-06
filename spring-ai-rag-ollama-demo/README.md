# spring-ai-rag-ollama-demo

Minimal-Demo fuer Retrieval-Augmented-Generation (RAG) mit **Spring AI 2.0**,
einem lokalen LLM ueber **Ollama** und **Qdrant** als Vektorspeicher.
Die Docker-Container fuer Ollama und Qdrant werden ueber die
**Docker-Compose-Integration von Spring AI** automatisch beim Anwendungsstart
hochgefahren und verbunden - es ist keine manuelle Host/Port-Konfiguration
in `application.yml` notwendig.

> Kurzanleitung mit Screenshots (Dokument indexieren, Frage stellen, ...):
> siehe [docs/02-anleitung.md](docs/02-anleitung.md).

## Projektstruktur

```
backend/   Spring Boot App (Java, pom.xml, compose.yaml)
frontend/  React-UI (Vite + TypeScript)
```

## Architektur

```
Client --HTTP--> Spring Boot App --ChatClient/QuestionAnswerAdvisor--> Ollama (LLM)
                                  \--VectorStore------------------------> Qdrant
```

- Dokumente werden per `TokenTextSplitter` in Chunks zerlegt und ueber das
  Ollama-Embedding-Modell (`mxbai-embed-large`) vektorisiert in Qdrant abgelegt.
- Fragen an `/api/chat/ask` laufen durch den `QuestionAnswerAdvisor`, der die
  passendsten Chunks aus Qdrant zieht und dem Prompt als Kontext mitgibt,
  bevor das Ollama-Chat-Modell (`llama3.2`) antwortet.

## Ollama

[Ollama](https://ollama.com) ist ein lokaler Server/Runtime fuer LLMs: Es laedt
Modell-Gewichte herunter, haelt sie im Speicher vor und stellt Chat- und
Embedding-Modelle ueber eine einheitliche REST-API (`http://localhost:11434`)
bereit - ein lokaler Ersatz fuer Cloud-APIs wie OpenAI, nur dass alles auf dem
eigenen Rechner laeuft. `http://localhost:11434` selbst zeigt nur einen
Status-Text ("Ollama is running") - kein GUI. Die eigentlichen API-Endpunkte
(`/api/generate`, `/api/chat`, `/api/embed`, Modell-Verwaltung, ...) sind in der
[offiziellen Ollama-API-Referenz](https://github.com/ollama/ollama/blob/main/docs/api.md)
dokumentiert.

In dieser Demo laufen zwei Modelle ueber dieselbe Ollama-Instanz:

| Rolle | Modell | Groesse | Parameter |
|---|---|---|---|
| Chat (Antworten generieren) | `llama3.2` | ~2,0 GB | 3B |
| Embedding (Text → Vektor fuer Qdrant) | `mxbai-embed-large` | ~669 MB | ~335M |

### Weitere Modelle laden

Zusaetzliche Chat-Modelle lassen sich jederzeit in denselben Ollama-Container
nachladen, z. B.:

```bash
docker exec -it $(docker ps -qf "ancestor=ollama/ollama") ollama pull mistral
docker exec -it $(docker ps -qf "ancestor=ollama/ollama") ollama pull gemma2
```

Welches Modell tatsaechlich fuer den Chat verwendet wird, steuert
`spring.ai.ollama.chat.model` in `application.yml` (siehe [Konfiguration](#konfiguration))
- nach dem Pull dort einfach den Modellnamen eintragen und die Anwendung neu starten.

**Hinweis:** Das Embedding-Modell (`mxbai-embed-large`) sollte nicht ohne
Weiteres gewechselt werden - die in Qdrant gespeicherten Vektoren sind an genau
dieses Modell gebunden. Ein anderes Embedding-Modell erzeugt einen inkompatiblen
Vektorraum, alle Dokumente muessten dann neu indexiert werden.

## Voraussetzungen

- Java 21+
- Maven 3.9+ (oder die IDE-eigene Maven-Integration)
- Docker Desktop / Docker Engine mit Compose-Plugin (laeuft und ist erreichbar)

## Start

1. Anwendung starten (im Verzeichnis `backend/`; Docker Compose wird von
   Spring Boot automatisch mitgestartet):

   ```bash
   cd backend
   mvn spring-boot:run
   ```

   Beim ersten Start dauert es etwas, weil die Images `ollama/ollama` und
   `qdrant/qdrant` gezogen werden. Alternativ vorher manuell hochfahren:

   ```bash
   cd backend
   docker compose up -d
   ```

2. **Modelle in Ollama laden** (einmalig, die Container-Volumes machen das
   persistent - nach dem ersten Pull entfaellt dieser Schritt):

   ```bash
   docker exec -it $(docker ps -qf "ancestor=ollama/ollama") ollama pull llama3.2
   docker exec -it $(docker ps -qf "ancestor=ollama/ollama") ollama pull mxbai-embed-large
   ```

   Kleineres Chat-Modell fuer schwaechere Hardware: `llama3.2:1b` (dann auch
   in `application.yml` unter `spring.ai.ollama.chat.model` anpassen).

3. Anwendung ist unter `http://localhost:8080` erreichbar.
   Swagger UI (OpenAPI) liegt unter `http://localhost:8080/swagger-ui.html`.

4. **React-Frontend** (separat, im Verzeichnis `frontend/`):

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

   Der Vite-Dev-Server läuft unter `http://localhost:5173` und leitet
   `/api`-Aufrufe per Proxy an das Backend auf Port 8080 weiter
   (siehe `frontend/vite.config.ts`) - es ist keine CORS-Konfiguration
   im Backend notwendig.

## Nutzung

**Dokument (Text) indexieren:**

```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{"content": "Spring AI ist ein Framework fuer die Integration von KI-Faehigkeiten in Spring-Anwendungen.", "source": "manual"}'
```

**Textdatei hochladen (z. B. .txt/.md):**

```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@./mein-dokument.txt"
```

**Frage stellen (RAG):**

```bash
curl -X POST http://localhost:8080/api/chat/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "Was ist Spring AI?"}'
```

## Konfiguration

Wichtige Einstellungen in `backend/src/main/resources/application.yml`:

| Property | Bedeutung |
|---|---|
| `spring.ai.ollama.chat.model` | Chat-Modell (Standard: `llama3.2`) |
| `spring.ai.ollama.embedding.model` | Embedding-Modell (Standard: `mxbai-embed-large`) |
| `spring.ai.vectorstore.qdrant.collection-name` | Qdrant-Collection (Standard: `rag-demo`) |

`base-url` (Ollama) sowie `host`/`port` (Qdrant) sind bewusst **nicht** gesetzt,
da sie von der Docker-Compose-Integration (`spring-ai-spring-boot-docker-compose`)
automatisch anhand der laufenden Container aus `compose.yaml` injiziert werden.
Laeuft Ollama/Qdrant stattdessen ausserhalb von Docker Compose, muessen diese
Werte manuell ergaenzt werden (z. B. `spring.ai.ollama.base-url: http://localhost:11434`).

## Erweiterungsideen

- **PDF/Office-Ingestion**: zusaetzliche Reader-Bibliothek einbinden
  (`spring-ai-pdf-document-reader` bzw. ein Tika-basierter Reader) statt
  reinem Klartext-Upload.
- **Qdrant-Dashboard**: `http://localhost:6333/dashboard` zeigt die indexierten
  Punkte/Collections.
- **Modulare RAG-Pipeline**: `QuestionAnswerAdvisor` durch
  `RetrievalAugmentationAdvisor` ersetzen, um Query-Rewriting, Multi-Query
  o.ae. zu ergaenzen (siehe Spring AI Referenzdokumentation zu RAG).
