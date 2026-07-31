# Text-to-Speech Demo

Spring-Backend (WebFlux, non-blocking) + React-Frontend. TTS via OpenAI (`tts-1`).

Konzept: [docs/anforderungen.md](docs/anforderungen.md)
Bebilderte Anleitung zu den beiden Anwendungsfällen (Text vorlesen, Dialog): [docs/anleitung.md](docs/anleitung.md)

## Backend starten

```
cd backend
$env:OPENAI_API_KEY = "sk-..."
.\mvnw.cmd spring-boot:run
```

Läuft auf http://localhost:8080. Erlaubte CORS-Origin und Modell/Stimme siehe `backend/src/main/resources/application.yml`.

## Frontend starten

```
cd frontend
npm install
npm run dev
```

Läuft auf http://localhost:5173, proxied `/api` auf das Backend (siehe `vite.config.js`).

## Endpoint

`POST /api/tts` mit `{ "text": "...", "voice": "alloy" }` → Antwort `audio/mpeg`.

Verfügbare Stimmen: `alloy`, `echo`, `fable`, `onyx`, `nova`, `shimmer`. Text max. 4096 Zeichen (OpenAI-Limit).
