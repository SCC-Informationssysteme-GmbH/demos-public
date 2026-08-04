package de.scc.jeebpeldemo.rest.dto;

import java.math.BigDecimal;

public class BestellungKorrekturRequest {

    private BigDecimal betrag;
    private String email;

    public BigDecimal getBetrag() {
        return betrag;
    }

    public void setBetrag(BigDecimal betrag) {
        this.betrag = betrag;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
