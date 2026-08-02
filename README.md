# demos-public

Öffentliche Sammlung ausgewählter Demo-Projekte der SCC Informationssysteme GmbH.

| Projekt | Beschreibung | Tech-Stack |
|---|---|---|
| [spring-tts-demo](spring-tts-demo/) | Text-to-Speech Demo: Text per OpenAI TTS in gesprochene Sprache umwandeln, inkl. Mehr-Stimmen-Dialog | Spring Boot (WebFlux), React, OpenAI TTS API |
| [spring-ai-demo](spring-ai-demo/) | Showcase mit sechs typischen LLM-Integrationsbereichen: REST-Chat, Prompt-Templates, RAG, Vektordatenbank (Qdrant), LangChain4j, KI-Business-Logik | Spring Boot, React, OpenAI API, Qdrant, LangChain4j |
| [spring-n8n-demo](spring-n8n-demo/) | Bestell-Freigabeprozess: Backend löst per Webhook einen n8n-Workflow aus, der ab einem Schwellwert eine Freigabe-Anfrage in Slack postet | Spring Boot, React, n8n, Slack |
| [spring-t23d-demo](spring-t23d-demo/) | Text-to-3D (T23D): Bauteil per Text beschreiben, LLM generiert FreeCAD-Python-Code, der isoliert in Docker ein 3D-Modell (STL) erzeugt und im Browser angezeigt wird | Spring Boot, React (react-three-fiber), OpenAI API, FreeCAD, Docker |

Jedes Projekt ist eigenständig und enthält eine eigene README mit Setup-Anleitung.

## Markenhinweise

Alle in diesem Repository genannten Produkt- und Firmennamen sind Marken oder eingetragene Marken der jeweiligen Eigentümer:

- **OpenAI, GPT, ChatGPT** – OpenAI OpCo, LLC.
- **Spring, Spring Boot** – Broadcom Inc. (VMware).
- **React** – Meta Platforms, Inc.
- **Qdrant** – Qdrant Solutions GmbH.
- **LangChain, LangChain4j** – LangChain, Inc.
- **n8n** – n8n GmbH.
- **Slack** – Slack Technologies, LLC (Salesforce).
- **FreeCAD** – FreeCAD-Projekt (Open Source, LGPL2+).
- Weitere genannte Produkt- und Firmennamen können Marken der jeweiligen Inhaber sein.

Die Verwendung dieser Marken dient ausschließlich der Identifikation von Produkten und Technologien und impliziert keine Partnerschaft, Empfehlung oder Zugehörigkeit zu den jeweiligen Rechteinhabern.

## Haftungsausschluss

Die Projekte in diesem Repository sind Demos und Lernbeispiele zum Ausprobieren – sie zeigen Konzepte, sind aber nicht für den Produktiveinsatz gedacht (u. a. ohne Authentifizierung und mit vereinfachter Fehlerbehandlung). Der Code wird "as is", ohne jegliche Gewährleistung bereitgestellt.

Wer den Code ausführt oder in eigenen Projekten verwendet, tut dies auf eigene Verantwortung. Für Schäden, die durch die Nutzung entstehen – etwa Kosten durch die Nutzung kostenpflichtiger externer Dienste (z. B. OpenAI-API-Aufrufe mit dem eigenen API-Key) – wird keine Haftung übernommen.
