package de.scc.jeebpeldemo.service;

/**
 * Ergebnis der fachlichen Bestellpruefung - entscheidet in BestellungFreigabeProcess.bpel
 * ueber den Pfad (Gelbfall bei !gueltig, sonst automatische/Manager-Freigabe je nach Betrag).
 */
public class PruefungsErgebnis {

    private final boolean gueltig;
    private final String fehlerCode;

    public PruefungsErgebnis(boolean gueltig, String fehlerCode) {
        this.gueltig = gueltig;
        this.fehlerCode = fehlerCode;
    }

    public boolean isGueltig() {
        return gueltig;
    }

    public String getFehlerCode() {
        return fehlerCode;
    }
}
