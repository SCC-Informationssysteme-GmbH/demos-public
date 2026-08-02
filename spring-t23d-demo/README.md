# Text-to-3D (T23D)

Web-Anwendung: Prompt links, interaktiver 3D-Viewer rechts. Der Prompt wird an OpenAI geschickt,
das daraus FreeCAD-Python-Code generiert; dieser Code laeuft isoliert in einem FreeCAD-Docker-Container
und erzeugt eine STL-Datei, die im Frontend angezeigt wird.

Anforderungen im Detail: [docs/01-anforderungen.md](docs/01-anforderungen.md).
Bebilderte Anleitung: [docs/02-anleitung.md](docs/02-anleitung.md).

## URLs im Überblick

| Was | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend (API-Basis) | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI-JSON | http://localhost:8080/v3/api-docs |
| Health-Check | http://localhost:8080/api/health |

## Projektstruktur

```
spring-t23d-demo/
├── backend/            Spring Boot 3 / Java 21 / Maven
├── freecad-docker/      Dockerfile fuer den FreeCAD-Container
├── frontend/            React + Vite, @react-three/fiber
├── docker-compose.yml   Gesamt-Setup (optional)
└── docs/
```

## Voraussetzungen

- Java 21, Node.js 20+, Docker Desktop
- OpenAI API-Key (`OPENAI_API_KEY`)

## Lokale Entwicklung (empfohlen)

FreeCAD-Image bauen:

```bash
docker build -t text-to-3d-freecad:latest freecad-docker
```

Backend starten (nativ, nicht containerisiert — `ProcessBuilder` ruft `docker run` direkt auf dem Host auf):

```bash
cd backend
$env:OPENAI_API_KEY = "sk-..."
./mvnw spring-boot:run
```

Frontend starten:

```bash
cd frontend
npm install
npm run dev
```

Frontend läuft auf `http://localhost:5173` und proxied `/api/*` auf das Backend (`http://localhost:8080`).

## Gesamt-Setup mit docker-compose

```bash
cp .env.example .env
# .env anpassen: OPENAI_API_KEY, HOST_DATA_DIR (absoluter Pfad, z. B. C:/git/demos/spring-t23d-demo/data)
docker compose up --build
```

**Hinweis:** Beim containerisierten Backend startet das Backend die FreeCAD-Container weiterhin per
Docker-Socket-Mount (`/var/run/docker.sock`) auf dem Host (Docker-outside-of-Docker). Damit der
Host-Docker-Daemon die Skript-Ordner korrekt mounten kann, muss `HOST_DATA_DIR` der tatsaechliche
Host-Pfad sein (nicht der Pfad *innerhalb* des Backend-Containers) — siehe `APP_FREECAD_SCRIPTSDIRHOST`
in `docker-compose.yml` und `backend/src/main/resources/application.yml`. Das ist eine fuer den
Demo-Zweck akzeptable Vereinfachung, keine produktionsreife Loesung (siehe Abschnitt 8 der Anforderungen).

## API

| Endpoint | Beschreibung |
|---|---|
| `POST /api/generate` | `{ "prompt": "..." }` → generiert Modell, liefert `{ id, modelUrl }` |
| `GET /api/models/{id}.stl` | liefert die generierte STL-Datei |
| `GET /api/health` | prueft Docker-Verfuegbarkeit |

## Sicherheit

Generierter Code wird vor der Ausfuehrung auf verdaechtige Muster geprueft (`import os`, `subprocess`,
`open(`, `socket`, `eval(`, `exec(`, ...) und laeuft ausschliesslich in einem isolierten Container ohne
Netzwerkzugriff (`--network none`) mit Ressourcenlimits und Timeout. Details siehe Anforderung 4.4.
