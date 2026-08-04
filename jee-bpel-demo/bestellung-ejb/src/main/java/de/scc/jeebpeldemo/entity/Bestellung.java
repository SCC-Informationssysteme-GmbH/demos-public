package de.scc.jeebpeldemo.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "BESTELLUNG")
public class Bestellung implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "kunde_id")
    private Kunde kunde;

    @Column(nullable = false)
    private LocalDateTime bestelldatum;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal betrag = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BestellungStatus status = BestellungStatus.NEU;

    @OneToMany(mappedBy = "bestellung", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Bestellposition> positionen = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Kunde getKunde() {
        return kunde;
    }

    public void setKunde(Kunde kunde) {
        this.kunde = kunde;
    }

    public LocalDateTime getBestelldatum() {
        return bestelldatum;
    }

    public void setBestelldatum(LocalDateTime bestelldatum) {
        this.bestelldatum = bestelldatum;
    }

    public BigDecimal getBetrag() {
        return betrag;
    }

    public void setBetrag(BigDecimal betrag) {
        this.betrag = betrag;
    }

    public BestellungStatus getStatus() {
        return status;
    }

    public void setStatus(BestellungStatus status) {
        this.status = status;
    }

    public List<Bestellposition> getPositionen() {
        return positionen;
    }

    public void setPositionen(List<Bestellposition> positionen) {
        this.positionen = positionen;
    }
}
