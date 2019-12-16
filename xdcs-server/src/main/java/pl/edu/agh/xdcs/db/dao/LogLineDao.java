package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.DbUtils;
import pl.edu.agh.xdcs.db.entity.AgentEntity;
import pl.edu.agh.xdcs.db.entity.LogLineEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
public class LogLineDao extends EntityDaoBase<LogLineEntity> {
    @Override
    protected Class<LogLineEntity> getEntityClass() {
        return LogLineEntity.class;
    }

    @SuppressWarnings("unchecked")
    public List<LogLineEntity> findByPeriod(String taskId, Instant from, Instant to) {
        return entityManager.createQuery("select ll from LogLine ll " +
                "where " +
                "ll.task.id = :taskId and " +
                "ll.time >= :f and " +
                "ll.time <= :t " +
                "order by ll.time")
                .setParameter("taskId", taskId)
                .setParameter("f", Optional.ofNullable(from).orElse(DbUtils.MIN_INSTANT))
                .setParameter("t", Optional.ofNullable(to).orElse(DbUtils.MAX_INSTANT))
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<LogLineEntity> findByAgents(String taskId, List<AgentEntity> agents) {
        return entityManager.createQuery("select ll from LogLine ll " +
                "where ll.loggedBy in :agents and " +
                "ll.task.id = :taskId " +
                "order by ll.time")
                .setParameter("agents", agents)
                .setParameter("taskId", taskId)
                .getResultList();
    }
}
