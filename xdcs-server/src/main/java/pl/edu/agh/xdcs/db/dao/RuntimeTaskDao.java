package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.DatabaseInconsistencyException;
import pl.edu.agh.xdcs.db.entity.RuntimeTaskEntity;

/**
 * @author Kamil Jarosz
 */
public class RuntimeTaskDao extends EntityDaoBase<RuntimeTaskEntity> {
    @Override
    protected Class<RuntimeTaskEntity> getEntityClass() {
        return RuntimeTaskEntity.class;
    }

    public void removeById(String taskId) {
        int deleted = entityManager.createQuery("delete from RuntimeTask rt where rt.id = :id")
                .setParameter("id", taskId)
                .executeUpdate();

        if (deleted != 1) {
            throw new DatabaseInconsistencyException("Expected a runtime task with ID " + taskId + " to exist");
        }
    }
}
