package de.scc.jeebpeldemo.rest.dto;

public class EntscheidungRequest {

    private Long aufgabeId;
    private boolean freigegeben;
    private String kommentar;

    public Long getAufgabeId() {
        return aufgabeId;
    }

    public void setAufgabeId(Long aufgabeId) {
        this.aufgabeId = aufgabeId;
    }

    public boolean isFreigegeben() {
        return freigegeben;
    }

    public void setFreigegeben(boolean freigegeben) {
        this.freigegeben = freigegeben;
    }

    public String getKommentar() {
        return kommentar;
    }

    public void setKommentar(String kommentar) {
        this.kommentar = kommentar;
    }
}
