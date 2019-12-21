package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.DatabaseInconsistencyException;
import pl.edu.agh.xdcs.db.entity.AgentEntity;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

/**
 * @author Krystian Życiński
 */
public class AgentDao extends EntityDaoBase<AgentEntity> {
    @Override
    protected Class<AgentEntity> getEntityClass() {
        return AgentEntity.class;
    }

    public Optional<AgentEntity> findByName(String agentName) {
        try {
            Object result = entityManager
                    .createQuery("select a from Agent a where a.name = :name")
                    .setParameter("name", agentName)
                    .getSingleResult();

            return Optional.of((AgentEntity) result);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public void setAgentStatus(String agentName, AgentEntity.Status status) {
        int updatedRows = entityManager.createQuery("update Agent a " +
                "set a.status = :status where a.name = :name")
                .setParameter("name", agentName)
                .setParameter("status", status)
                .executeUpdate();

        if (updatedRows != 1) {
            throw new DatabaseInconsistencyException(
                    "Expected one agent with name " + agentName + ", got " + updatedRows);
        }
    }

    @SuppressWarnings("unchecked")
    public List<AgentEntity> getAllAgents() {
        return entityManager.createQuery("select a from Agent a")
                .getResultList();
    }
}
