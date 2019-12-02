package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.entity.HistoricalTaskEntity;
import pl.edu.agh.xdcs.db.entity.QueuedTaskEntity;
import pl.edu.agh.xdcs.db.entity.RuntimeTaskEntity;
import pl.edu.agh.xdcs.db.entity.Task;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
public class TaskDao extends DaoBase {
    private Optional<Task> mapResultsToTask(Object[] results) {
        HistoricalTaskEntity historicalTask = (HistoricalTaskEntity) results[0];
        RuntimeTaskEntity runtimeTask = (RuntimeTaskEntity) results[1];
        QueuedTaskEntity queuedTask = (QueuedTaskEntity) results[2];

        if (runtimeTask != null) {
            return Optional.of(runtimeTask);
        }

        if (queuedTask != null) {
            return Optional.of(queuedTask);
        }

        if (historicalTask != null) {
            return Optional.of(historicalTask);
        }

        return Optional.empty();
    }

    public Optional<Task> findById(String taskId) {
        try {
            Object[] results = (Object[]) entityManager
                    .createQuery("select h, r, q " +
                            "from HisTask h " +
                            "left join RuntimeTask r on h.id = r.id " +
                            "left join TaskQueue q on h.id = q.id " +
                            "where h.id = :taskId")
                    .setParameter("taskId", taskId)
                    .getSingleResult();

            return mapResultsToTask(results);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Task> list(int firstResult, int maxResults) {
        Stream<Object[]> resultStream = entityManager
                .createQuery("select h, r, q " +
                        "from HisTask h " +
                        "left join RuntimeTask r on h.id = r.id " +
                        "left join TaskQueue q on h.id = q.id")
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .getResultStream();
        return resultStream.map(this::mapResultsToTask)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
