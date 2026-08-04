package de.scc.jeebpeldemo.repository;

import de.scc.jeebpeldemo.entity.Entscheidung;
import de.scc.jeebpeldemo.entity.Freigabeaufgabe;
import de.scc.jeebpeldemo.entity.FreigabeaufgabeTyp;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class FreigabeaufgabeRepository {

    @PersistenceContext(unitName = "bestellungPU")
    private EntityManager entityManager;

    public Freigabeaufgabe anlegen(Freigabeaufgabe aufgabe) {
        entityManager.persist(aufgabe);
        return aufgabe;
    }

    public Freigabeaufgabe finden(Long id) {
        return entityManager.find(Freigabeaufgabe.class, id);
    }

    public List<Freigabeaufgabe> findenOffeneNachTyp(FreigabeaufgabeTyp typ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Freigabeaufgabe> query = cb.createQuery(Freigabeaufgabe.class);
        Root<Freigabeaufgabe> root = query.from(Freigabeaufgabe.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("typ"), typ));
        predicates.add(cb.isFalse(root.get("erledigt")));

        query.select(root)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.asc(root.get("erstelltAm")));

        return entityManager.createQuery(query).getResultList();
    }

    /**
     * Die aktuell offene Aufgabe eines Typs zu einer Bestellung - genau eine erwartet,
     * da der BPEL-Prozess je Pfad/Eskalationsstufe immer nur eine Aufgabe gleichzeitig offen haelt.
     */
    public Freigabeaufgabe findenAktuelleOffene(Long bestellungId, FreigabeaufgabeTyp typ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Freigabeaufgabe> query = cb.createQuery(Freigabeaufgabe.class);
        Root<Freigabeaufgabe> root = query.from(Freigabeaufgabe.class);

        query.select(root)
                .where(
                        cb.equal(root.get("bestellung").get("id"), bestellungId),
                        cb.equal(root.get("typ"), typ),
                        cb.isFalse(root.get("erledigt"))
                )
                .orderBy(cb.desc(root.get("erstelltAm")));

        List<Freigabeaufgabe> ergebnis = entityManager.createQuery(query).setMaxResults(1).getResultList();
        return ergebnis.isEmpty() ? null : ergebnis.get(0);
    }

    public void entscheiden(Long aufgabeId, Entscheidung entscheidung, String kommentar) {
        Freigabeaufgabe aufgabe = finden(aufgabeId);
        if (aufgabe == null) {
            throw new NoResultException("Freigabeaufgabe " + aufgabeId + " nicht gefunden");
        }
        aufgabe.setEntscheidung(entscheidung);
        aufgabe.setKommentar(kommentar);
        aufgabe.setErledigt(true);
    }

    public void eskalieren(Long aufgabeId) {
        Freigabeaufgabe aufgabe = finden(aufgabeId);
        if (aufgabe != null) {
            aufgabe.setEskaliert(true);
            aufgabe.setErledigt(true);
        }
    }
}
