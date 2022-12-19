package mc.obliviate.rocketparkour.database.repository;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import mc.obliviate.rocketparkour.parkour.AbstractParkour;
import mc.obliviate.rocketparkour.statistics.ParkourStatistic;
import org.bukkit.scoreboard.Criteria;
import org.hibernate.Session;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StatisticRepository extends Repository {

    public StatisticRepository() {
        super("rocketparkour");
        super.annotatedClasses(ParkourStatistic.class);
        super.connect();
    }

    public void save(ParkourStatistic statistics) {
        super.session.getTransaction().begin();
        super.session.persist(statistics);
        super.session.getTransaction().commit();
    }

    public void delete(ParkourStatistic statistics) {
        super.session.getTransaction().begin();
        super.session.remove(statistics);
        super.session.getTransaction().commit();
    }

    public List<ParkourStatistic> query(UUID playerUniqueId) {
        var builder = super.session.getCriteriaBuilder();
        var cr = builder.createQuery(ParkourStatistic.class);
        var root = cr.from(ParkourStatistic.class);
        cr.select(root).where(builder.equal(root.get("playerUniqueId"), playerUniqueId))
                .orderBy(builder.asc(root.get("duration")));
        return super.session.createQuery(cr).getResultList();
    }

    public List<ParkourStatistic> query(AbstractParkour parkour) {
        var builder = super.session.getCriteriaBuilder();
        var cr = builder.createQuery(ParkourStatistic.class);
        var root = cr.from(ParkourStatistic.class);
        cr.select(root).where(builder.equal(root.get("parkourName"), parkour.getParkourName()))
                .orderBy(builder.asc(root.get("duration")));
        return super.session.createQuery(cr).getResultList();
    }

    public List<ParkourStatistic> query(UUID playerUniqueId, AbstractParkour parkour) {
        var builder = super.session.getCriteriaBuilder();
        var cr = builder.createQuery(ParkourStatistic.class);
        var root = cr.from(ParkourStatistic.class);
        cr.select(root).where(
                builder.equal(root.get("playerUniqueId"), playerUniqueId),
                builder.equal(root.get("parkourName"), parkour.getParkourName()))
                .orderBy(builder.asc(root.get("duration")));
        return super.session.createQuery(cr).getResultList();
    }


    public Optional<ParkourStatistic> findByUID(UUID uid) {
        return Optional.ofNullable(super.session.get(ParkourStatistic.class, uid));
    }

    public ParkourStatistic getByUID(UUID uid) {
        return this.findByUID(uid).orElseThrow(() -> new RuntimeException("statistics not found!"));
    }

    public List<ParkourStatistic> findAll() {
        return super.session.createQuery("SELECT a FROM statistics a", ParkourStatistic.class).getResultList();
    }

}