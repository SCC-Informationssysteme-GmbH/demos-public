package de.scc.jeebpeldemo.repository;

import de.scc.jeebpeldemo.entity.Bestellung;
import de.scc.jeebpeldemo.entity.BestellungStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class BestellungRepository {

    @PersistenceContext(unitName = "bestellungPU")
    private EntityManager entityManager;

    public Bestellung anlegen(Bestellung bestellung) {
        entityManager.persist(bestellung);
        return bestellung;
    }

    public Bestellung finden(Long id) {
        return entityManager.find(Bestellung.class, id);
    }

    public List<Bestellung> suchen(BestellungSuchfilter filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Bestellung> query = cb.createQuery(Bestellung.class);
        Root<Bestellung> root = query.from(Bestellung.class);

        List<Predicate> predicates = new ArrayList<>();
        if (filter.getKundeId() != null) {
            predicates.add(cb.equal(root.get("kunde").get("id"), filter.getKundeId()));
        }
        if (filter.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getVon() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("bestelldatum"), filter.getVon()));
        }
        if (filter.getBis() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("bestelldatum"), filter.getBis()));
        }

        query.select(root)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(root.get("bestelldatum")));

        return entityManager.createQuery(query).getResultList();
    }

    public void statusAktualisieren(Long id, BestellungStatus status) {
        Bestellung bestellung = finden(id);
        if (bestellung != null) {
            bestellung.setStatus(status);
        }
    }
}
