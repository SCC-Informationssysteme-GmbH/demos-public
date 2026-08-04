package de.scc.jeebpeldemo.entity;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sachbearbeiter-/Manager-Arbeitsvorrat: ein Eintrag pro offener Gelbfall- oder
 * Manager-Freigabeentscheidung, die der BPEL-Prozess ueber ein {@code pick} erwartet.
 */
@Entity
@Table(name = "FREIGABEAUFGABE")
public class Freigabeaufgabe implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bestellung_id")
    private Bestellung bestellung;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FreigabeaufgabeTyp typ;

    @Column(nullable = false)
    private String grund;

    @Column(nullable = false)
    private LocalDateTime erstelltAm;

    @Column(nullable = false)
    private boolean erledigt;

    @Enumerated(EnumType.STRING)
    private Entscheidung entscheidung;

    private String kommentar;

    @Column(nullable = false)
    private boolean eskaliert;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonbTransient
    public Bestellung getBestellung() {
        return bestellung;
    }

    public void setBestellung(Bestellung bestellung) {
        this.bestellung = bestellung;
    }

    @Transient
    private Long bestellungIdOhneBeziehung;

    public Long getBestellungId() {
        return bestellung == null ? bestellungIdOhneBeziehung : bestellung.getId();
    }

    /**
     * Nur fuer JSON-B-Deserialisierung auf der Client-Seite (ApiClient): dort kommt
     * die Entity ohne die (serverseitig per @JsonbTransient unterdrueckte) bestellung-Beziehung
     * an, sodass getBestellungId() sonst immer null zurueckgeben wuerde.
     */
    public void setBestellungId(Long bestellungId) {
        this.bestellungIdOhneBeziehung = bestellungId;
    }

    @Transient
    private String bestellungKundeNameOhneBeziehung;

    public String getBestellungKundeName() {
        if (bestellung == null) {
            return bestellungKundeNameOhneBeziehung;
        }
        return bestellung.getKunde() == null ? null : bestellung.getKunde().getName();
    }

    public void setBestellungKundeName(String bestellungKundeName) {
        this.bestellungKundeNameOhneBeziehung = bestellungKundeName;
    }

    @Transient
    private String bestellungKundeEmailOhneBeziehung;

    public String getBestellungKundeEmail() {
        if (bestellung == null) {
            return bestellungKundeEmailOhneBeziehung;
        }
        return bestellung.getKunde() == null ? null : bestellung.getKunde().getEmail();
    }

    public void setBestellungKundeEmail(String bestellungKundeEmail) {
        this.bestellungKundeEmailOhneBeziehung = bestellungKundeEmail;
    }

    @Transient
    private BigDecimal bestellungBetragOhneBeziehung;

    public BigDecimal getBestellungBetrag() {
        return bestellung == null ? bestellungBetragOhneBeziehung : bestellung.getBetrag();
    }

    public void setBestellungBetrag(BigDecimal bestellungBetrag) {
        this.bestellungBetragOhneBeziehung = bestellungBetrag;
    }

    public FreigabeaufgabeTyp getTyp() {
        return typ;
    }

    public void setTyp(FreigabeaufgabeTyp typ) {
        this.typ = typ;
    }

    public String getGrund() {
        return grund;
    }

    public void setGrund(String grund) {
        this.grund = grund;
    }

    public LocalDateTime getErstelltAm() {
        return erstelltAm;
    }

    public void setErstelltAm(LocalDateTime erstelltAm) {
        this.erstelltAm = erstelltAm;
    }

    public boolean isErledigt() {
        return erledigt;
    }

    public void setErledigt(boolean erledigt) {
        this.erledigt = erledigt;
    }

    public Entscheidung getEntscheidung() {
        return entscheidung;
    }

    public void setEntscheidung(Entscheidung entscheidung) {
        this.entscheidung = entscheidung;
    }

    public String getKommentar() {
        return kommentar;
    }

    public void setKommentar(String kommentar) {
        this.kommentar = kommentar;
    }

    public boolean isEskaliert() {
        return eskaliert;
    }

    public void setEskaliert(boolean eskaliert) {
        this.eskaliert = eskaliert;
    }
}
