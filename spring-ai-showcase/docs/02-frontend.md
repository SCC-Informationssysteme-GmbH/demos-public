# Frontend-Konzept: Signal-Konsole

## Grundidee

Die App inszeniert die 6 LLM-Integrationspunkte als **Kanäle (CH.01–CH.06)** an einem
Patch-Panel, wie in einem alten Tonstudio oder einer Leitwarte. Jeder Kanal ist ein
eigener Endpunkt/Service im Backend. Ein dünner, animierter **Signalweg** oben auf der
Seite zeigt live, welcher Kanal gerade eine Anfrage ans Backend schickt und wann die
Antwort zurückkommt — das ist gleichzeitig Navigation und Statusanzeige.

Look & Feel: dunkel, technisch, reduziert — bewusst kein Standard-KI-Demo-Look
(kein Creme/Terracotta, keine runden Soft-UI-Karten). Eher Richtung Mess- und
Regeltechnik: scharfe Kanten, Monospace-Codes, klare Statuszustände (idle / sendet /
empfängt / Fehler).

## Kanal-Mapping

| Kanal | Backend-Modul   | Thema                                                 |
|-------|-----------------|--------------------------------------------------------|
| CH.01 | `llmrest`       | LLM per REST (OpenAI / Azure OpenAI)                   |
| CH.02 | `prompt`        | Prompt-Orchestrierung / Prompt-Templates               |
| CH.03 | `rag`           | RAG — Embeddings + Vektorsuche                         |
| CH.04 | `vectordb`      | Vektordatenbank-Integration (Pinecone/Qdrant/…)        |
| CH.05 | `langchain`     | LangChain4J-Service                                     |
| CH.06 | `businesslogic` | KI-gestützte Business-Logik (Klassifikation, Summary)  |

Jeder Kanal bekommt im Frontend eine eigene Seite/Route (`/ch/01` … `/ch/06`), die im
Patch-Panel als anwählbarer Slot dargestellt wird.

## Visuelle Sprache

- **Hintergrund**: near-black (kein reines Schwarz), leicht texturiert wie eine
  Frontplatte.
- **Ein Akzentton** für den aktiven Signalweg (z. B. warmes Amber oder Cyan) — signalisiert
  "hier läuft gerade ein Request". Restliche Palette bewusst zurückhaltend (Grautöne),
  damit der Signalweg als Signature-Element nicht mit anderen Farbflächen konkurriert.
- **Statusfarben**: neutral/grau = idle, Akzentton pulsierend = aktive Anfrage, Rot/Warnton
  = Fehler (z. B. API-Key fehlt, Backend nicht erreichbar).
- Konkrete Hex-Werte werden beim Umsetzen der Artefakte final über die `dataviz`/
  `artifact-design`-Skill-Konventionen abgestimmt (Kontrast, Light/Dark-Tauglichkeit).

### Typografie

| Rolle                          | Font           |
|--------------------------------|----------------|
| Headlines / Kanal-Titel        | Space Grotesk  |
| Kanal-Codes, JSON-Output, Logs | IBM Plex Mono  |
| Fließtext / Beschreibungen     | Inter          |

## Signature-Element: die Signalleitung

Eine dünne horizontale Linie quer über den oberen Bereich der Seite, die den aktuell
aktiven Kanal per animiertem Impuls anzeigt (Request raus → Impuls läuft zum
Kanal-Slot → Antwort da → Impuls läuft zurück). Dient als:

- visuelles Feedback für laufende Requests (Ladezustand ersetzt klassische Spinner),
- Navigationshilfe (Klick auf einen Kanal-Slot in der Leitung springt zur jeweiligen Seite),
- Fehleranzeige (Impuls bricht ab / färbt sich rot bei Backend-Fehlern).

## Seitenstruktur

- **Übersichtsseite** ("Patch-Panel"): alle 6 Kanäle als Slots, Signalweg oben,
  Kurzbeschreibung + Status je Kanal.
- **Kanal-Detailseite** (`/ch/0X`): Eingabeformular für den jeweiligen Use Case
  (z. B. Prompt-Eingabe, Dokument-Upload für RAG), Request/Response-Anzeige im
  Mono-Font-Stil (wie ein Terminal-Log), Link zurück zum Patch-Panel.

## Technische Umsetzung (Kurzhinweis)

- React + Vite (siehe `01-ueberblick.md`/Architekturentscheidung).
- Animation der Signalleitung z. B. mit Framer Motion oder CSS-Keyframes, kein
  schweres Animations-Framework nötig.
- Routing über `react-router-dom`, ein Ordner `src/pages/ch01` … `ch06` je Kanal.
- Request-Status (idle/loading/success/error) pro Kanal als einfacher React-State,
  der die Signalweg-Komponente treibt.