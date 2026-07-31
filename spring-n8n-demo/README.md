# spring-n8n-demo

Demo eines Bestell-Prozesses mit Spring Boot Backend, React-Frontend und
n8n-Workflow-Automatisierung. Bestellungen ab 500 EUR erfordern eine
Freigabe, die als Slack-Nachricht angekündigt wird; darunter erfolgt
automatische Freigabe.

Details zu den Anforderungen: [docs/anforderungen.md](docs/anforderungen.md).
Bebilderte Schritt-für-Schritt-Anleitung: [docs/anleitung.md](docs/anleitung.md).

## Architektur

```
Frontend (React) → Backend (Spring Boot) → Webhook → n8n → Slack-Nachricht
                                                    ↘ (< 500 EUR) Auto-Approve-Callback → Backend

Slack-Button (Genehmigen/Ablehnen) → n8n-Webhook (slack-interaction) → Callback → Backend
```

- **backend/**: Spring Boot REST-API, verwaltet Bestellungen, ruft n8n-Webhook auf.
- **frontend/**: React-SPA mit Formular zum Anlegen und Historie der Bestellungen.
- **docker-compose.yml**: n8n-Container.
- **docs/n8n-order-approval-workflow.json**: Export des n8n-Workflows zum Re-Import.

### Freigabe-Rückweg (Slack-Buttons)

Der Workflow enthält einen zweiten Webhook (`Webhook: slack-interaction`), der
als **Interactivity Request URL** in der Slack-App hinterlegt wird. Klickt
ein Nutzer auf "Genehmigen"/"Ablehnen", sendet Slack den Interaction-Payload
an diesen Webhook. Der Node "Slack-Payload parsen" liest `action_id`
(`approve_order`/`reject_order`) und die Bestell-ID aus dem Payload und
übergibt sie an den Node "Callback: Freigabe/Ablehnung", der wie beim
Auto-Approve `POST /api/orders/{id}/callback` am Backend aufruft – nur mit
der Entscheidung aus dem Slack-Klick statt fest auf `APPROVED`.

## Voraussetzungen

- Java 21, Maven (Wrapper `mvnw` ist enthalten)
- Node.js 22+
- Docker Desktop

## Starten

**1. n8n starten**

```bash
docker compose up -d
```

n8n ist danach unter http://localhost:5678 erreichbar. Beim ersten Start muss
der Workflow importiert werden:

```bash
docker cp docs/n8n-order-approval-workflow.json spring-n8n-demo-n8n:/tmp/workflow.json
docker exec spring-n8n-demo-n8n n8n import:workflow --input=/tmp/workflow.json
docker exec spring-n8n-demo-n8n n8n publish:workflow --id=orderApproval1
docker restart spring-n8n-demo-n8n
```

Danach in der n8n-UI unter **Credentials** ein Slack-API-Credential mit
Bot-Token anlegen und im Workflow-Node "Slack: Freigabe anfragen" zuweisen
(siehe [docs/anforderungen.md](docs/anforderungen.md) für Details zur
Slack-App-Einrichtung).

**2. Backend starten**

```bash
cd backend
./mvnw spring-boot:run
```

Läuft auf http://localhost:8081. Swagger UI: http://localhost:8081/swagger-ui.html

**3. Frontend starten**

```bash
cd frontend
npm install
npm run dev
```

Läuft auf http://localhost:5173.

## Bekannte Einschränkungen

Die Slack-Nachricht enthält zwei Buttons ("Genehmigen"/"Ablehnen"). Der
Workflow verarbeitet deren Klicks über den Webhook `slack-interaction`
(siehe oben) – ohne öffentlich erreichbaren Tunnel (z.B. ngrok) kann Slack
lokal laufendes n8n aber nicht erreichen, da die Interactivity Request URL
in der Slack-App auf eine öffentliche URL zeigen muss. Ohne Tunnel erfolgt
die Freigabe daher weiterhin manuell über die Swagger UI
(`POST /api/orders/{id}/callback` mit `{"decision":"APPROVED"}` oder
`{"decision":"REJECTED"}`).
