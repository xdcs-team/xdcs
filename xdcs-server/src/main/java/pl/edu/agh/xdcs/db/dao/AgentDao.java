package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.entity.AgentEntity;

import javax.persistence.NoResultException;
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
}
