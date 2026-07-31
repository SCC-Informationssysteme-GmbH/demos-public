# Anforderungen: Bestell-Prozess mit n8n-Integration

## Ziel
Demo eines Spring Boot Backends, das Bestellungen entgegennimmt und den
Freigabe-Prozess per Webhook an n8n auslagert. Ab einem Schwellwert wird
eine Freigabe-Nachricht in Slack gepostet, darunter erfolgt automatische
Freigabe.

## Ablauf

1. **Bestellung anlegen**: Client sendet Bestellung an das Backend, Status `NEW`.
2. **Webhook an n8n**: Backend ruft n8n-Webhook auf, Status wechselt auf `PENDING_APPROVAL`.
3. **n8n-Workflow**: prüft den Betrag.
   - Betrag < 500 EUR: ruft direkt den Callback mit `APPROVED` auf.
   - Betrag >= 500 EUR: postet eine Freigabe-Nachricht in Slack (Channel `#genehmigungen`).
4. **Freigabe**: Klick auf "Genehmigen"/"Ablehnen" in Slack löst den Webhook
   `slack-interaction` in n8n aus, der die Entscheidung an den Callback-Endpunkt
   weiterreicht. Ohne öffentlichen Tunnel zu n8n erfolgt die Freigabe ersatzweise
   manuell über den Callback-Endpunkt (siehe [Bekannte Einschränkungen](#bekannte-einschränkungen)).
5. **Status abfragen**: Client kann den Bestellstatus jederzeit abfragen.

## Backend-Anforderungen

**Fachlich**
- Bestellung: ID, Artikel, Menge, Betrag, Kunde, Status, Zeitstempel.
- Status-Werte: `NEW`, `PENDING_APPROVAL`, `APPROVED`, `REJECTED`.
- Schwellwert für Freigabe-Pflicht: 500 EUR.

**Technisch**
- Spring Boot 4.1.0, Java 21, Maven.
- REST-Endpunkte:
  - `POST /api/orders` – Bestellung anlegen
  - `GET /api/orders` – alle Bestellungen
  - `GET /api/orders/{id}` – einzelne Bestellung
  - `POST /api/orders/{id}/callback` – Freigabe-Entscheidung setzen (`{"decision":"APPROVED"|"REJECTED"}`)
- Persistenz: H2 (in-memory). Tabellenname bewusst `orders` statt `order`, da `ORDER` ein reserviertes SQL-Keyword ist.
- n8n-Webhook-Aufruf über `RestClient`, HTTP/1.1 fest konfiguriert (siehe Einschränkungen).
- OpenAPI/Swagger UI unter `/swagger-ui.html`.
- Kein Auth im Demo-Scope.
- Konfiguration über `application.yml` (Port, n8n-Webhook-URL).

## Frontend-Anforderungen

**Fachlich**
- Bestellung anlegen (Formular: Artikel, Menge, Betrag, Kunde).
- Bestellhistorie mit Status einsehen.

**Technisch**
- React-SPA (Vite, TypeScript), eigenes Verzeichnis `frontend/`.
- React Router mit Sidebar-Navigation, zwei Seiten:
  - **Neue Bestellung**: Formular, Erfolgsmeldung mit Link zur Historie.
  - **Historie**: Tabelle mit farbigen Status-Badges, Beträge als EUR formatiert.
- Kommunikation über REST mit dem Backend (CORS aktiviert), Backend-URL fest auf `http://localhost:8081`.

## n8n-Anforderungen

- n8n lokal via Docker Compose (`docker-compose.yml`), Port 5678.
- Workflow "Order Approval" (exportiert unter `docs/n8n-order-approval-workflow.json`):
  - Webhook-Trigger `order-created`.
  - IF-Node: Betrag >= 500 EUR.
  - Slack-Node: Nachricht mit Bestelldetails und zwei Buttons ("Genehmigen"/"Ablehnen") im Block-Kit-Format.
  - HTTP-Request-Node: Auto-Approve-Callback bei Beträgen unter 500 EUR.
  - Webhook-Trigger `slack-interaction`: nimmt Slack-Interactivity-Payload entgegen.
  - Code-Node: parst `action_id`/Bestell-ID aus dem Payload, mappt auf `APPROVED`/`REJECTED`.
  - HTTP-Request-Node: ruft mit der geparsten Entscheidung denselben Callback-Endpunkt auf.
- Slack-Anbindung: eigene Slack-App mit Bot-Token (Scope `chat:write` + `chat:write:bot`), Credential manuell in n8n hinterlegt, Bot im Channel `#genehmigungen` eingeladen.

## Bekannte Einschränkungen

- **Button-Interaktion erreicht Slack nicht**: Der Workflow-Node `slack-interaction`
  verarbeitet Button-Klicks korrekt, aber n8n läuft lokal auf `localhost` und ist von
  Slack aus nicht erreichbar. Ohne öffentlichen Tunnel kann Slack den Interactivity-Request
  nicht zustellen. Freigabe erfolgt daher aktuell manuell über die Swagger UI
  (`POST /api/orders/{id}/callback`). Siehe [Erweiterungen](#erweiterungen) für den Tunnel-Ansatz.
- **n8n-HTTP-Client-Hinweis**: Der Standard-JDK-HttpClient von Spring versucht per Default einen
  HTTP/2-Upgrade, der gegen den n8n-Webhook hängen bleibt. Der `RestClient` ist deshalb explizit
  auf HTTP/1.1 mit Timeouts konfiguriert (siehe `WebClientConfig`).

## Erweiterungen

- **Öffentlicher Tunnel für Slack-Interaktivität** (noch nicht umgesetzt): Damit Slack
  Button-Klicks tatsächlich an n8n zustellen kann, braucht es einen öffentlich erreichbaren
  Tunnel zum lokalen n8n, z.B. mit [ngrok](https://ngrok.com/):
  1. `ngrok http 5678` starten – liefert eine öffentliche HTTPS-URL (z.B. `https://abc123.ngrok.io`).
  2. In der Slack-App unter **Interactivity & Shortcuts** die Request URL auf
     `https://abc123.ngrok.io/webhook/slack-interaction` setzen.
  3. Slack kann Button-Klicks danach direkt an den n8n-Webhook `slack-interaction` senden,
     der Callback-Endpunkt am Backend wird automatisch mit der Entscheidung aufgerufen.

  Einschränkung der kostenlosen ngrok-Version: Die URL ändert sich bei jedem Neustart und
  müsste in der Slack-App jedes Mal neu hinterlegt werden. Für einen dauerhaften Betrieb
  wäre ein statischer ngrok-Domain-Plan oder ein alternativer Tunnel-Dienst nötig.

## Projektstruktur

```
spring-n8n-demo/
├── docs/
│   ├── anforderungen.md
│   └── n8n-order-approval-workflow.json
├── backend/        # Spring Boot (REST, JPA, H2, n8n-Webhook-Client, Swagger)
├── frontend/       # React-SPA (Vite/TypeScript, React Router)
└── docker-compose.yml  # n8n-Container
```
