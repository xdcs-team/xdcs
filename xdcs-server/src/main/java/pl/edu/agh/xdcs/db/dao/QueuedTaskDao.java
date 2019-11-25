package pl.edu.agh.xdcs.db.dao;

import com.google.common.base.Preconditions;
import pl.edu.agh.xdcs.db.entity.BaseEntity;
import pl.edu.agh.xdcs.db.entity.QueuedTaskEntity;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
public class QueuedTaskDao extends EntityDaoBase<QueuedTaskEntity> {
    @Override
    protected Class<QueuedTaskEntity> getEntityClass() {
        return QueuedTaskEntity.class;
    }

    @SuppressWarnings("unchecked")
    public List<QueuedTaskEntity> checkCandidates(int maxResults, Duration minWaitTime) {
        Preconditions.checkArgument(maxResults > 0);
        Objects.requireNonNull(minWaitTime);
        Instant now = Instant.now();

        List<QueuedTaskEntity> tasks = entityManager
                .createQuery("select q from TaskQueue as q " +
                        "where q.lastCheck < :maxLastCheck " +
                        "order by q.created asc")
                .setParameter("maxLastCheck", now.minus(minWaitTime))
                .setMaxResults(maxResults)
                .getResultList();

        List<String> ids = tasks.stream()
                .map(BaseEntity::getId)
                .collect(Collectors.toList());

        entityManager
                .createQuery("update TaskQueue as q " +
                        "set q.lastCheck = :lastChecked " +
                        "where q.id in :ids")
                .setParameter("lastChecked", now)
                .setParameter("ids", ids)
                .executeUpdate();
        return tasks;
    }
}
