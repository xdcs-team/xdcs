package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;

import java.util.List;

/**
 * @author Kamil Jarosz
 */
public class TaskDefinitionDao extends DaoBase<TaskDefinitionEntity> {
    @Override
    protected Class<TaskDefinitionEntity> getEntityClass() {
        return TaskDefinitionEntity.class;
    }

    @SuppressWarnings("unchecked")
    public List<TaskDefinitionEntity> listTaskDefinitions() {
        return entityManager
                .createQuery("select td from TaskDefinitionEntity as td")
                .getResultList();
    }
}
