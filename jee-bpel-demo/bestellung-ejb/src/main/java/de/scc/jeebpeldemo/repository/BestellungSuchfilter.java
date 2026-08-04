package de.scc.jeebpeldemo.repository;

import de.scc.jeebpeldemo.entity.BestellungStatus;

import java.time.LocalDateTime;

public class BestellungSuchfilter {

    private Long kundeId;
    private BestellungStatus status;
    private LocalDateTime von;
    private LocalDateTime bis;

    public Long getKundeId() {
        return kundeId;
    }

    public void setKundeId(Long kundeId) {
        this.kundeId = kundeId;
    }

    public BestellungStatus getStatus() {
        return status;
    }

    public void setStatus(BestellungStatus status) {
        this.status = status;
    }

    public LocalDateTime getVon() {
        return von;
    }

    public void setVon(LocalDateTime von) {
        this.von = von;
    }

    public LocalDateTime getBis() {
        return bis;
    }

    public void setBis(LocalDateTime bis) {
        this.bis = bis;
    }
}
