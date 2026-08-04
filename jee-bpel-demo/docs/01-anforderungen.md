# Fachlicher Hintergrund: Bestellfreigabe

Die Demo modelliert einen Bestellfreigabeprozess, wie er in Unternehmen typisch ist: Eine
Bestellung wird erfasst, geprüft und je nach Prüfergebnis und Höhe unterschiedlich weiter
behandelt. Der Ablauf ist absichtlich so gewählt, dass er die sprachlichen Mittel von BPEL
ausreizt und nicht nur einen einzelnen Aufruf orchestriert.

## Fachliche Regeln

1. **Prüfung.** Nach dem Prozessstart prüft der Prozess die Bestellung. Ein Betrag kleiner
   oder gleich null ergibt den Fehlercode `UNGUELTIGER_BETRAG`, eine nicht zum Muster
   passende E-Mail-Adresse `UNGUELTIGE_MAIL_ADRESSE`.
2. **Pfad 1 – Gelbfallbearbeitung.** Schlägt die Prüfung fehl, entsteht eine
   Gelbfall-Aufgabe. Ein Sachbearbeiter entscheidet, ob die Bestellung freigegeben oder
   abgelehnt wird; er kann sie vorher korrigieren.
3. **Pfad 2 – automatische Freigabe.** Ist die Bestellung valide und der Betrag kleiner
   als 10000, gibt der Prozess sie ohne manuellen Eingriff frei.
4. **Pfad 3 – Manager-Freigabe.** Ab einem Betrag von 10000 greift das
   Vier-Augen-Prinzip: Der Prozess erzeugt eine Freigabeaufgabe für einen Manager und
   wartet auf dessen Entscheidung. Bleibt sie aus, eskaliert der Prozess nach Ablauf einer
   Frist und eröffnet die Bestellung automatisch als Gelbfall.
5. **Abbruch jederzeit.** Eine Bestellung kann in jedem laufenden Zustand storniert
   werden. Bereits erzeugte Freigabeaufgaben werden dabei kompensiert, damit keine
   verwaisten Aufgaben zurückbleiben.

Den vollständigen Statusfluss inklusive aller Übergänge und Fristen zeigt das
Zustandsdiagramm in der [README](../README.md).

## Was daran technisch interessant ist

Aus diesen Regeln ergeben sich genau die BPEL-Konstrukte, die die Demo zeigen soll:

| Fachliche Anforderung | BPEL-Mittel |
|---|---|
| Warten auf eine menschliche Entscheidung | `receive`/`onMessage` mit Korrelationsset |
| Entscheidung *oder* Fristablauf | `pick` mit `onMessage` und `onAlarm` |
| Verzweigung nach Prüfergebnis und Betrag | verschachtelte `if`/`else` über Prozessvariablen |
| Storno zu jedem Zeitpunkt | `eventHandlers` mit `onEvent` plus `throw` |
| Rücknahme bereits erzeugter Aufgaben | `compensationHandler` im `scope`, ausgelöst per `compensate` |
| Technischer Fehler als Sicherheitsnetz | `faultHandlers` mit `catch` |

Die technische Umsetzung dieser Punkte beschreibt [02-architektur.md](02-architektur.md),
die Bedienung der Anwendung [03-anleitung.md](03-anleitung.md).
