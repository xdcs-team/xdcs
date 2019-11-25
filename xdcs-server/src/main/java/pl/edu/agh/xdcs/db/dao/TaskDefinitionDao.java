package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;

import java.util.List;

/**
 * @author Kamil Jarosz
 */
public class TaskDefinitionDao extends EntityDaoBase<TaskDefinitionEntity> {
    @Override
    protected Class<TaskDefinitionEntity> getEntityClass() {
        return TaskDefinitionEntity.class;
    }

    @SuppressWarnings("unchecked")
    public List<TaskDefinitionEntity> listTaskDefinitions(int from, int limit) {
        return entityManager
                .createQuery("select td from TaskDefinition as td")
                .setFirstResult(from)
                .setMaxResults(limit)
                .getResultList();
    }
}
