package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.entity.LogLineEntity;

import java.time.Instant;
import java.util.List;

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
                "(:from is null or ll.time >= :from) and " +
                "(:to is null or ll.time <= :to)")
                .setParameter("taskId", taskId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }
}
