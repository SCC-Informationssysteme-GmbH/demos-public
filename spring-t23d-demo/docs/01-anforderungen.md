# Projekt: Text-to-3D mit Spring Boot, FreeCAD und React

## 1. Ziel

Eine Web-Anwendung mit zwei Bereichen:

- **Links:** Texteingabe (Prompt), in dem der Nutzer ein 3D-Objekt beschreibt (z. B. "Ein Flansch mit 80mm Durchmesser und 4 Bohrungen").
- **Rechts:** Interaktiver 3D-Viewer, der das generierte Modell anzeigt und per Maus gedreht, gezoomt und verschoben werden kann.

Der Prompt wird an ein LLM (Claude oder OpenAI) geschickt, das daraus FreeCAD-Python-Code generiert. Dieser Code wird headless in einem FreeCAD-Docker-Container ausgeführt und erzeugt eine STL-Datei, die im Frontend angezeigt wird.

## 2. Architektur-Überblick

```
[React Frontend]  --REST-->  [Spring Boot Backend]  --Prompt-->  [LLM API (Claude/OpenAI)]
                                     |
                                     | generiert Python-Skript
                                     v
                              [FreeCAD Docker Container]
                                     |
                                     | exportiert STL
                                     v
                              [Spring Boot liest STL]
                                     |
                                     v
                              [React lädt STL via react-three-fiber]
```

## 3. Tech-Stack

| Schicht | Technologie |
|---|---|
| Backend | Spring Boot 3.x, Java 21, Maven |
| CAD-Ausführung | FreeCAD (headless, `freecadcmd`) in eigenem Docker-Container |
| LLM-Integration | OpenAI API (implementiert) — Claude als Provider vorgesehen (`AppProperties.Llm.Provider.CLAUDE`), aber aktuell nicht implementiert (`LlmConfig` wirft `IllegalStateException`) |
| Frontend | React (Vite), `@react-three/fiber`, `@react-three/drei` |
| Datenformat | STL (Austausch Backend → Frontend) |
| Containerisierung | Docker (für FreeCAD), optional docker-compose für Gesamt-Setup |

## 4. Backend-Anforderungen (Spring Boot)

### 4.1 REST-Endpoints

- `POST /api/generate`
  - Request Body: `{ "prompt": "string" }`
  - Ablauf:
    1. Prompt an LLM-API senden mit striktem System-Prompt (siehe 4.3)
    2. Generierten Python-Code validieren (siehe 4.4 Sicherheit)
    3. Code in temporäre Datei schreiben
    4. FreeCAD-Container per `ProcessBuilder` starten (`docker run ...`), Skript-Ordner als Volume mounten
    5. Timeout einhalten (z. B. 30 Sekunden), danach Prozess abbrechen
    6. Erzeugte STL-Datei einlesen
    7. Response: STL-Datei als Binary (`Content-Type: model/stl`) oder Ablage unter `/api/models/{id}.stl` mit URL-Rückgabe
  - Fehlerfälle: LLM-Fehler, Timeout, FreeCAD-Fehler (Skript wirft Exception), leere/fehlerhafte STL-Ausgabe — jeweils mit aussagekräftiger Fehlermeldung an Frontend

- `GET /api/models/{id}.stl`
  - Liefert eine zuvor generierte STL-Datei aus (falls Ablage-Variante statt Direct-Response gewählt wird)

- `GET /api/health` (optional)
  - Prüft, ob Docker/FreeCAD-Container verfügbar ist

### 4.2 Konfiguration

- `application.yml` / `application.properties`:
  - LLM-Provider auswählbar (`claude` oder `openai`)
  - API-Keys aus Umgebungsvariablen (`ANTHROPIC_API_KEY`, `OPENAI_API_KEY`) — **niemals im Code oder in Properties-Dateien im Klartext**
  - Timeout-Werte für Docker-Ausführung
  - Pfad für temporäre Skript- und Output-Dateien

### 4.3 System-Prompt für LLM (Vorschlag, im Backend hinterlegt)

Der System-Prompt muss das LLM anweisen:

- Nur FreeCAD-Python-API-Code zu erzeugen (`FreeCAD`, `Part`, `Draft` Module)
- Ein Dokumentobjekt zu erzeugen und am Ende als `/work/output.stl` zu exportieren
- Keine Dateisystem-Zugriffe außerhalb von `/work`
- Keine Netzwerkzugriffe, keine Shell-Aufrufe, keine Imports außerhalb von FreeCAD-Standardmodulen
- Nur reinen Code zurückzugeben, ohne Markdown-Codeblock-Umrandung oder Erklärtext

### 4.4 Sicherheitsanforderungen (wichtig, da LLM-generierter Code ausgeführt wird)

- Ausführung **ausschließlich** im isolierten Docker-Container
- Container ohne Netzwerkzugriff starten (`--network none`)
- Nur der Skript-Ordner wird gemountet, kein Zugriff auf restliches Dateisystem
- Ressourcenlimits setzen (CPU/Memory), z. B. `--memory=512m --cpus=1`
- Timeout auf Prozessebene (z. B. 30s), harter Kill bei Überschreitung
- Vor Ausführung: einfache statische Prüfung des generierten Codes auf verdächtige Muster (z. B. `import os`, `subprocess`, `open(`, `socket`) — bei Treffer Ablehnung mit Fehlermeldung
- Kein `eval`/`exec` von Nutzereingaben im Backend selbst

### 4.5 FreeCAD-Container

- Eigenes schlankes Dockerfile (kein Rückgriff auf veraltete Community-Images):

```dockerfile
FROM debian:bookworm-slim
RUN apt-get update && apt-get install -y freecad --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /work
ENTRYPOINT ["freecadcmd"]
```

- Aufruf-Beispiel:

```bash
docker run --rm --network none --memory=512m --cpus=1 \
  -v /tmp/scripts/<id>:/work \
  text-to-3d-freecad:latest /work/model.py
```

## 5. Frontend-Anforderungen (React)

### 5.1 Layout

- Zwei-Spalten-Layout (CSS Grid oder Flexbox), responsive
- **Linke Spalte:**
  - Textarea für Prompt-Eingabe
  - Button "Generieren"
  - Ladeindikator während Backend-Anfrage läuft
  - Fehleranzeige bei fehlgeschlagener Generierung
  - Verlauf der letzten Prompts (lokaler State), vorbelegt mit Beispiel-Prompts aus `frontend/src/data/examplePrompts.json`; je Eintrag Icons zum Übernehmen/Löschen
- **Rechte Spalte:**
  - `<Canvas>` von `@react-three/fiber`
  - STL-Modell laden via `useLoader(STLLoader, url)`
  - `OrbitControls` aus `@react-three/drei` für Drehen/Zoomen/Pan
  - Einfaches Lichtsetup: `ambientLight` + `directionalLight`
  - Platzhalter-Anzeige, solange kein Modell generiert wurde
  - Kamera passt sich nach dem Laden automatisch an die Objektgröße an (Autofit anhand der Bounding Sphere)
  - Werkzeugleiste: Zoom/Reset, Drahtgitter, Hintergrund schwarz/weiß umschaltbar, Gitter ein-/ausblendbar, Maße-Anzeige (Bounding-Box X/Y/Z in mm), Vollbild, Screenshot-Export (PNG), STL-Download
- Draggbarer Splitter zwischen linker und rechter Spalte; die rechte Spalte scrollt bei zu schmalem Fenster, statt die Seite in die Breite zu drücken

### 5.2 Technische Anforderungen

- Vite als Build-Tool
- Abhängigkeiten: `three`, `@react-three/fiber`, `@react-three/drei`
- API-Aufruf an Backend (`POST /api/generate`), STL als Blob empfangen und per `URL.createObjectURL` an den Loader übergeben
- Fehler- und Ladezustände sauber im UI abbilden (kein stiller Fehlschlag)

### 5.3 Erweiterbarkeit (spätere Ausbaustufen, nicht Teil des ersten Durchlaufs)

- Mehrere Modelle nebeneinander vergleichen
- Parameter-Schieberegler zur Nachbearbeitung ohne neuen Prompt
- ~~Export-Button (STL-Download)~~ — umgesetzt: STL-Download und PNG-Screenshot-Export (STEP-Export weiterhin offen)
- Verlauf mit Wiederherstellung früherer Modelle

## 6. Projektstruktur (Vorschlag)

```
text-to-3d/
├── backend/                 # Spring Boot Projekt
│   ├── src/main/java/...
│   ├── src/main/resources/application.yml
│   └── pom.xml
├── freecad-docker/
│   └── Dockerfile
├── frontend/                 # React + Vite Projekt
│   ├── src/
│   │   ├── App.jsx
│   │   ├── components/
│   │   │   ├── PromptPanel.jsx
│   │   │   └── ModelViewer.jsx
│   │   └── main.jsx
│   ├── package.json
│   └── vite.config.js
├── docker-compose.yml        # optional: Gesamt-Setup
└── README.md
```

## 7. Entscheidungen für die Umsetzung

- [x] LLM-Provider: **OpenAI** als primärer Provider
- [x] STL-Rückgabe: **Ablage + URL** — STL wird unter `/api/models/{id}.stl` abgelegt, `POST /api/generate` liefert die URL zurück
- [x] docker-compose: **Ja** — orchestriert Backend + FreeCAD-Image für den Gesamtstart; das Backend ruft den FreeCAD-Container pro Request weiterhin selbst per `ProcessBuilder`/`docker run` auf
- [x] Persistenz: **Einfache Datei-Persistenz ohne Datenbank** — generierte STL-Dateien bleiben dauerhaft unter dem Modell-Verzeichnis liegen (kein automatisches Cleanup, keine DB)

## 8. Nicht-Ziele (explizit aus Scope ausgeschlossen)

- Keine Nutzerverwaltung/Login im ersten Durchlauf
- Keine Bearbeitung bestehender Modelle per Prompt (nur Neu-Generierung)
- Keine GUI-Darstellung von FreeCAD selbst (nur headless CLI-Nutzung)
- Keine produktive Härtung/Skalierung (Fokus liegt auf funktionsfähigem Beispielprojekt)
