package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.DatabaseInconsistencyException;
import pl.edu.agh.xdcs.db.entity.HistoricalTaskEntity;
import pl.edu.agh.xdcs.db.entity.Task.Result;

/**
 * @author Kamil Jarosz
 */
public class HistoricalTaskDao extends EntityDaoBase<HistoricalTaskEntity> {
    @Override
    protected Class<HistoricalTaskEntity> getEntityClass() {
        return HistoricalTaskEntity.class;
    }

    public void setFinished(String taskId) {
        setResult(taskId, Result.FINISHED);
    }

    public void setErrored(String taskId) {
        setResult(taskId, Result.ERRORED);
    }

    private void setResult(String taskId, Result result) {
        int rowsChanged = entityManager.createQuery("update HisTask ht set ht.result = :result where ht.id = :id")
                .setParameter("result", result)
                .setParameter("id", taskId)
                .executeUpdate();

        if (rowsChanged != 1) {
            throw new DatabaseInconsistencyException("Cannot set result for task with ID " + taskId);
        }
    }
}
