# Architektur: jee-bpel-demo

Ziel: Weiterentwicklung von `jee-ejb-demo` (Schwesterprojekt) mit einem sprachlich viel
reichhaltigeren BPEL-Prozess: statt eines einzelnen `receive→invoke→reply` orchestriert
`BestellungFreigabeProcess` drei fachliche Freigabepfade (Gelbfallbearbeitung, automatische
Freigabe, Manager-Freigabe mit Eskalation) inkl. Korrelationssets, `pick`/`onAlarm`,
Kompensation und einem echten Abbruch-Pattern (`eventHandlers` + Fault). WebSphere ist durch
WildFly ersetzt, BPEL läuft weiterhin auf Apache ODE. Alles läuft lokal per Docker, keine
WebSphere-Lizenz nötig.

## Bedienungsanleitung / Aufruf-URLs

### Starten

```bash
cd docker
docker compose up -d --build   # bauen + starten
docker compose down            # stoppen
```

Der Compose-Stack hat einen expliziten Projektnamen (`name: jee-bpel-demo` in
`docker-compose.yml`), damit er nicht mit dem Schwesterprojekt `jee-ejb-demo` kollidiert
(dessen `docker-compose.yml` liegt ebenfalls in einem Ordner namens `docker` - ohne
expliziten Namen würden beide Projekte denselben Compose-Projektnamen und damit
Container-/Volume-Namen verwenden und sich gegenseitig überschreiben).

### Alle URLs im Überblick

| Was | URL | Methode |
|---|---|---|
| Bestellungen-Liste (REST) | `http://localhost:9081/bestellung/api/bestellungen` | GET |
| Bestellung anlegen (REST) | `http://localhost:9081/bestellung/api/bestellungen` | POST (JSON) |
| Prozess starten | `http://localhost:9081/bestellung/api/bestellungen/{id}/prozess/start` | POST |
| Prozess stornieren | `http://localhost:9081/bestellung/api/bestellungen/{id}/prozess/stornieren` | POST (JSON: `grund`) |
| Gelbfall-Entscheidung | `http://localhost:9081/bestellung/api/bestellungen/{id}/prozess/gelbfall-entscheidung` | POST (JSON: `aufgabeId`,`freigegeben`,`kommentar`) |
| Manager-Entscheidung | `http://localhost:9081/bestellung/api/bestellungen/{id}/prozess/manager-entscheidung` | POST (JSON: `aufgabeId`,`freigegeben`,`kommentar`) |
| Offene Gelbfälle | `http://localhost:9081/bestellung/api/freigabeaufgaben/gelbfall` | GET |
| Offene Manager-Freigaben | `http://localhost:9081/bestellung/api/freigabeaufgaben/manager` | GET |
| GUI (JSF/PrimeFaces) | `http://localhost:9081/bestellung/bestellungen.xhtml` | Browser |
| GUI Gelbfallbearbeitung | `http://localhost:9081/bestellung/gelbfall.xhtml` | Browser |
| GUI Manager-Freigabe | `http://localhost:9081/bestellung/managerfreigabe.xhtml` | Browser |
| SOAP Prüfung (WSDL) | `http://localhost:9081/bestellung-ejb/BestellungPruefungService/BestellungPruefungWebService?wsdl` | GET |
| SOAP Gelbfall (WSDL) | `http://localhost:9081/bestellung-ejb/GelbfallService/GelbfallWebService?wsdl` | GET |
| SOAP Freigabe (WSDL) | `http://localhost:9081/bestellung-ejb/BestellungFreigabeService/BestellungFreigabeWebService?wsdl` | GET |
| BPEL-Prozessaufruf (ODE, direktes SOAP) | `http://localhost:8181/ode/processes/BestellungFreigabeProcess` | POST (SOAP) |
| ODE-Web-Konsole (Prozessliste) | `http://localhost:8181/ode/#/processes` | Browser |
| ODE-Web-Konsole (Instanzen) | `http://localhost:8181/ode/#/instances?q=` | Browser |
| Oracle-DB (JDBC) | `jdbc:oracle:thin:@localhost:1522/FREEPDB1` (User `bestellung`/`bestellung`) | JDBC |

Drei Zugriffsarten auf den Prozess sind technisch möglich (wie gefordert): **GUI** (JSF ruft
die REST-Fassade), **REST** (dieselbe Fassade, direkt per curl/Postman) und **direktes SOAP**
gegen den ODE-Endpunkt (Contract siehe `bestellung-bpel/src/main/bpel/BestellungFreigabeProcess.wsdl`).

### Beispiel-curl-Aufrufe

```bash
# Bestellung anlegen (Betrag < 10000 -> automatische Freigabe)
curl -X POST http://localhost:9081/bestellung/api/bestellungen \
  -H "Content-Type: application/json" \
  -d '{"kunde":{"name":"Max Mustermann","email":"max@example.com"},"betrag":500.00,"positionen":[]}'

# Prozess starten (id aus der Antwort oben)
curl -X POST http://localhost:9081/bestellung/api/bestellungen/1/prozess/start

# Gelbfall-Entscheidung (aufgabeId aus GET .../freigabeaufgaben/gelbfall)
curl -X POST http://localhost:9081/bestellung/api/bestellungen/2/prozess/gelbfall-entscheidung \
  -H "Content-Type: application/json" \
  -d '{"aufgabeId":1,"freigegeben":true,"kommentar":"Betrag manuell bestaetigt"}'

# Stornieren (funktioniert waehrend Gelbfall-/Manager-Wartezeit)
curl -X POST http://localhost:9081/bestellung/api/bestellungen/3/prozess/stornieren \
  -H "Content-Type: application/json" -d '{"grund":"Kunde storniert"}'
```

## Domäne

Bestellverwaltung wie im Schwesterprojekt (`Kunde`, `Bestellung`, `Bestellposition`, `Artikel`),
erweitert um `Bestellung.betrag` (Grundlage der 10000er-Schwelle) und `Freigabeaufgabe`
(Sachbearbeiter-/Manager-Arbeitsvorrat, ein Entity für Gelbfall **und** Managerfreigabe,
unterschieden per `typ`).

`BestellungStatus`: `NEU → IN_PRUEFUNG` (JMS-Event nach dem Anlegen, wie im Schwesterprojekt)
`→` je nach Pfad `GELBFALL | MANAGER_FREIGABE → ESKALIERT` (nur bei Timeout) `→ FREIGEGEBEN |
ABGELEHNT`, jederzeit `→ STORNIERT` (Abbruch), `FEHLER` als technischer Sicherheitsnetz-Pfad.

### Ablauf: `BestellungFreigabeProcess` (Apache ODE)

**Ein** Prozess, drei Pfade als Scopes innerhalb eines `if`/`else`, kein Split in
Subprozesse (Korrelation/Pick/Kompensation lassen sich vollständig in einem Prozess zeigen;
ein Subprozess müsste dieselbe Async-Logik nur redundant verdoppeln):

```
receive bestellungAnlegen (createInstance, Korrelation "BestellungKorrelation" auf bestellungId)
  → sofortige Ack-Reply (Prozess laeuft danach asynchron weiter, ggf. lange)

scope "Ablauf" (faultHandlers + eventHandlers fuer Abbruch)
  eventHandlers: onEvent bestellungStornieren → reply + throw bestellungStorniertFault
  faultHandlers: catch bestellungStorniertFault → compensate + statusAktualisieren(STORNIERT)
                 catchAll → statusAktualisieren(FEHLER)

  invoke pruefungPartner.pruefeBestellung

  if !gueltig
    scope "Gelbfall": invoke gelbfallEroeffnen (setzt Status GELBFALL serverseitig)
                       pick onMessage gelbfallEntscheidung → reply + statusAktualisieren(FREIGEGEBEN|ABGELEHNT)
  else if betrag < 10000
    scope "AutoFreigabe" (compensationHandler: kompensationFreigeben)
                       invoke automatischFreigeben (setzt Status FREIGEGEBEN serverseitig)
  else
    scope "ManagerFreigabe" (compensationHandler: kompensationFreigeben)
                       invoke managerFreigabeAnfordern (setzt Status MANAGER_FREIGABE serverseitig)
                       pick onMessage managerEntscheidung → reply + statusAktualisieren(...)
                            onAlarm PT2M → eskaliere (Status ESKALIERT) →
                              scope "EskalationAlsGelbfall": invoke gelbfallEroeffnen(grund=Timeout)
                                pick onMessage gelbfallEntscheidung → ...
                                     onAlarm PT1M (Fallback) → statusAktualisieren(ABGELEHNT)
```

Kein finales `<reply>` mit dem Endergebnis: `bestellungAnlegen` bekommt nur die sofortige Ack.
Das Ergebnis läuft über `statusAktualisieren`-Aufrufe zurück nach WildFly (System of Record) -
ODE bleibt reiner Orchestrator. `gelbfallEroeffnen`/`automatischFreigeben`/
`managerFreigabeAnfordern`/`managerFreigabeEskalieren` setzen den Status jeweils **serverseitig
selbst** (in der jeweiligen Business-Service-Methode) - der explizite `statusAktualisieren`-Aufruf
aus BPEL wird nur für die **finalen** Entscheidungen (Gelbfall-/Manager-Entscheidung, Storno,
Fehler) gebraucht, um Redundanz zu vermeiden.

**Kompensation**: `AutoFreigabe`- und `ManagerFreigabe`-Scope registrieren je einen
`compensationHandler` (`kompensationFreigeben`, setzt `FREIGEGEBEN → STORNIERT` zurück), da
nur diese beiden Pfade tatsächlich eine Freigabe *gewähren*. Hinweis: Da die jeweilige Scope
nach erteilter Freigabe sehr schnell abschliesst, ist das praktische Zeitfenster für eine
Kompensation (Storno *nach* bereits erteilter Freigabe, aber *bevor* der Scope vollständig
beendet ist) schmal - das Pattern ist strukturell korrekt demonstriert, ein breiteres
Zeitfenster wäre nur mit einer künstlichen Wartezeit nach Freigabe erreichbar (kein
Selbstzweck, daher bewusst nicht eingebaut).

## Technologie-Mapping

| Anforderung | Umsetzung |
|---|---|
| Java EE / Jakarta EE | Jakarta EE 10, Vollprofil |
| JPA/Hibernate | Hibernate, Entities `Bestellung`/`Kunde`/`Artikel`/`Bestellposition`/`Freigabeaufgabe` |
| CDI | `@Inject`, EJB/CDI-Zusammenspiel |
| EJB | `@Stateless` Service-Fassaden, `@MessageDriven` (MDB) für JMS |
| JMS | Queue `OrderEventsQueue` (Artemis), Status NEU→IN_PRUEFUNG async |
| REST | JAX-RS: CRUD + Prozess-Fassade + Freigabeaufgaben-Queues |
| SOAP | 3 JAX-WS-Partnerservices (Prüfung/Gelbfall/Freigabe), vom BPEL-Prozess aufgerufen |
| GUI | Jakarta Faces 4.0 (Mojarra) + PrimeFaces 13, ruft REST-Fassade |
| Relationale DB (Oracle) + Criteria API | Oracle Free (Docker), Criteria-API-Suchfilter |
| Maven | Multi-Modul-Build (ejb/web/ear/bpel/it) |
| JUnit/Mockito | Unit-Tests für alle Business-Services (Statuslogik der 3 Pfade) |
| Application Server | WildFly (Docker) |
| BPEL | Apache ODE, mehrwegiger Prozess mit Korrelation/Pick/Eskalation/Kompensation |

## Modulstruktur

```
jee-bpel-demo/
├── pom.xml
├── bestellung-ejb/     (Entities, Repository, Services, 3 SOAP-Endpoints, JMS)
├── bestellung-web/     (REST-Fassade Richtung ODE + JSF/PrimeFaces-GUI)
├── bestellung-ear/     (EAR: verpackt ejb+web)
├── bestellung-bpel/    (WSDLs + BestellungFreigabeProcess.bpel + deploy.xml)
├── bestellung-it/      (Integrationstests, nicht im Standard-Reactor)
├── docker/
│   ├── docker-compose.yml   (oracle-db, wildfly, ode; expliziter Projektname)
│   ├── wildfly/              (Dockerfile, Oracle-Modul, configure.cli)
│   └── ode/                  (Dockerfile, CORS-Fragment)
└── docs/
```

## Testprotokoll: End-to-End über den laufenden Docker-Stack

Alle vier Pfade wurden gegen den echten, laufenden Stack (nicht nur Unit-Tests) durchgespielt:

1. ✅ **Automatische Freigabe** (Betrag 500 < 10000): Bestellung anlegen → `IN_PRUEFUNG` (JMS)
   → Prozess starten → Status sofort `FREIGEGEBEN`.
2. ✅ **Gelbfall** (Betrag 0, Prüfung schlägt fehl mit `UNGUELTIGER_BETRAG`): Status → `GELBFALL`,
   Aufgabe erscheint in `/freigabeaufgaben/gelbfall`, Entscheidung → Status `FREIGEGEBEN`.
3. ✅ **Manager-Freigabe** (Betrag 15000 >= 10000): Status → `MANAGER_FREIGABE`, Aufgabe in
   `/freigabeaufgaben/manager`, Entscheidung → Status `FREIGEGEBEN`.
4. ✅ **Storno** (während offener Gelbfallbearbeitung): Status → `STORNIERT` (eventHandlers +
   Fault-Pattern funktioniert auch mitten in einem laufenden `pick`).
5. ✅ **Eskalation** (Betrag 20000, keine Managerentscheidung innerhalb `PT2M`): Timeout löst
   `managerFreigabeEskalieren` aus (Status `ESKALIERT`), Fall wird automatisch als Gelbfall mit
   Grund "Manager-Freigabe-Timeout - eskaliert" neu eröffnet.

### Automatisierte Integrationstests (`bestellung-it`)

```bash
mvn -f bestellung-it/pom.xml verify -Dit.wildflyBaseUri=http://localhost:9081/bestellung \
    -Dit.odeEndpoint=http://localhost:8181/ode/processes/BestellungFreigabeProcess
```

**Ergebnis: alle 5 Tests grün** gegen den laufenden Docker-Stack: `BestellungAutoFreigabeIT`,
`BestellungGelbfallIT`, `BestellungManagerFreigabeIT`, `BestellungStornoIT` (alle über die
REST-Fassade) sowie `BestellungFreigabeProcessDirectSoapIT` (Prozessstart per direktem
SOAP-Aufruf an ODE, ohne REST-Fassade - belegt den dritten geforderten Zugriffsweg).

### Bei der Inbetriebnahme gefundene und behobene Fehler

1. **ODE 1.3.8 lehnt `<onEvent>` mit `variable`-Referenz ohne eigenes `messageType`-Attribut ab**
   (`[VariableDeclMissingType]`, obwohl die referenzierte `<variable>` korrekt mit `messageType`
   deklariert war). Fix: `messageType` zusätzlich direkt auf `<onEvent>` angeben (Redundanz zur
   Variablendeklaration, aber von ODE offenbar für `eventHandlers` gesondert ausgewertet).
2. **`propertyAlias/@query` ist in WS-BPEL 2.0 kein Attribut, sondern ein Kind-Element**
   (`<vprop:query>...</vprop:query>`, siehe `ws-bpel_varprop.xsd`). Ein `query="..."`-Attribut
   wird von ODE stillschweigend ignoriert (kein Compile-Fehler) - Symptom war ein
   Korrelationswert `"nullnull"` in der ODE-Instanzübersicht (per PMAPI/`listAllInstances`
   diagnostiziert) und dadurch endlos hängende Folgenachrichten (`initiate="no"`), die erst nach
   dem generischen Axis2-MEX-Timeout (120s) mit einem kryptischen Fehler abbrachen. Fix: alle
   vier `propertyAlias`-Definitionen auf das Kind-Element-Format umgestellt.
3. **Echter Engine-Bug in ODE 1.3.8** (`EH_EVENT$WAITING.onRequestRcvd`,
   `ClassCastException: OSequence cannot be cast to OScope`): eine `<onEvent>`-Aktivität, die
   direkt eine `<sequence>` ist (statt eines `<scope>`), lässt den internen Event-Handler-Automaten
   abstürzen. Fix: die Sequence in ein zusätzliches (funktional leeres) `<scope>` gewrappt.
4. **JAX-WS-Client-Proxy nicht threadsicher wiederverwendbar**: ein einzelner, in einer
   `@ApplicationScoped`-CDI-Bean gecachter `Service.getPort(...)`-Proxy führte bei
   Wiederverwendung über mehrere Request-Threads zu unerklärlich hängenden Aufrufen. Fix:
   `BestellungOdeClient` baut pro Aufruf einen frischen Port (kein Caching) - geringer Overhead,
   aber garantiert threadsicher.
5. **Docker-Compose-Projektnamenskollision**: `jee-ejb-demo` und `jee-bpel-demo` haben ihre
   `docker-compose.yml` jeweils in einem Ordner namens `docker` liegen - ohne explizites
   `name:` verwendet Compose denselben Default-Projektnamen ("docker") für beide, wodurch
   Container und insbesondere das Oracle-Datenvolume zwischen den unabhängigen Projekten geteilt
   worden wären. Fix: `name: jee-bpel-demo` + eigener Volume-Name in `docker-compose.yml`.

## Nächste Schritte / offene Punkte

- `bestellung-it`: automatisierte Integrationstests (REST → alle 3 Pfade → BPEL-Roundtrip →
  Storno), analog zum manuellen Testprotokoll oben.
- WildFly-Management-Konsole (Port 9991) ist vom Host aus nicht erreichbar (wie im
  Schwesterprojekt, bindet nur an `127.0.0.1` im Container) - für diese Demo nicht benötigt.
