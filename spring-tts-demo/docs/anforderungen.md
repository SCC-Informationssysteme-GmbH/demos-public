# Konzept: Text-to-Speech App mit Spring Backend

Eine Architekturskizze für eine App, die Text über ein Frontend entgegennimmt, per Spring-Backend an einen Text-to-Speech-Dienst weiterleitet und das Ergebnis als Audio abspielt.

## Überblick

```
Frontend (Text)
   → POST /api/tts { text: "Hallo Welt" }
Backend (Spring)
   → WebClient.post() an TTS-API mit Text + Auth-Header
   ← Audio-Bytes (z. B. audio/mpeg)
Backend
   → Response an Frontend mit Content-Type audio/mpeg
Frontend
   → Blob erzeugen, in <audio> laden, abspielen
```

## 1. Frontend

- Texteingabefeld (React, Vue, Angular oder einfaches HTML + JS)
- Sendet den Text per HTTP (REST) an das Backend, z. B. `POST /api/tts`
- Empfängt entweder eine Audiodatei (Stream) oder eine URL zu einer generierten Datei
- Spielt das Audio über ein `<audio>`-Element ab

## 2. Spring Backend

- Controller nimmt den Text entgegen (`@PostMapping("/api/tts")`)
- Validiert und bereinigt den Text (Längenbegrenzung, ggf. Rate-Limiting)
- Ruft die TTS-API des gewählten Anbieters auf (per `WebClient` oder `RestTemplate`)
- Erhält Audiodaten zurück (meist MP3/WAV als Byte-Stream oder Base64)
- Gibt die Audiodaten als Response zurück oder speichert sie temporär und liefert eine URL

## 3. TTS-Anbieter (extern)

- API-Key wird ausschließlich serverseitig gespeichert
- Anbieter erhält Text + Stimmen-Parameter (Sprache, Stimme, Tempo, Tonhöhe)
- Liefert synthetisierte Audiodaten zurück

Beispiele für Anbieter: ElevenLabs, Amazon Polly, Google Cloud Text-to-Speech, Azure Speech, OpenAI TTS.

## Wichtige Design-Entscheidungen

### Synchron vs. asynchron

- **Kurze Texte:** synchroner Call reicht, Antwort kommt direkt
- **Lange Texte:** asynchron mit Job-Queue (z. B. Spring + RabbitMQ/Kafka), Frontend pollt oder erhält WebSocket-Update, sobald das Audio fertig ist

### Streaming

Manche TTS-APIs unterstützen Audio-Streaming in Chunks, sodass das Backend nicht auf die komplette Datei warten muss. Das verbessert die Latenz bei längeren Texten.

### Caching

Gleicher Text + gleiche Stimme → Ergebnis cachen (z. B. Redis oder S3). Spart Kosten und Zeit bei wiederholten Anfragen.

### Sicherheit

- API-Keys nur im Backend, niemals im Client-Code
- Rate-Limiting pro User/IP, um Kosten beim TTS-Anbieter zu kontrollieren
- Input-Validierung gegen Missbrauch (z. B. übermäßig lange Texte)

### Speicherung

- Entweder Audio transient verarbeiten und direkt streamen
- Oder in Object Storage (S3, MinIO) ablegen und eine URL zurückgeben, falls Wiederverwendung gewünscht ist

## Offene Fragen für die Umsetzung

- Welcher TTS-Anbieter soll konkret verwendet werden?
- Werden mehrere Sprachen/Stimmen benötigt?
- Soll es eine Nutzerverwaltung geben (z. B. Kontingente pro User)?
- Wie lang dürfen die Texte maximal sein?
