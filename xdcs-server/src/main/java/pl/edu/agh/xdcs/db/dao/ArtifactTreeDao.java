package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.entity.AgentEntity;
import pl.edu.agh.xdcs.db.entity.ArtifactTreeEntity;
import pl.edu.agh.xdcs.db.entity.Task;

import java.util.List;

/**
 * @author Kamil Jarosz
 */
public class ArtifactTreeDao extends EntityDaoBase<ArtifactTreeEntity> {
    @Override
    protected Class<ArtifactTreeEntity> getEntityClass() {
        return ArtifactTreeEntity.class;
    }

    public ArtifactTreeEntity find(Task task, AgentEntity agent) {
        return (ArtifactTreeEntity) entityManager.createQuery("select a from ArtifactTree a " +
                "where " +
                "a.task = :task and " +
                "a.uploadedBy = :agent " +
                "order by a.time")
                .setParameter("task", task)
                .setParameter("agent", agent)
                .getSingleResult();
    }

    @SuppressWarnings("unchecked")
    public List<ArtifactTreeEntity> query(Task task, List<AgentEntity> agents) {
        return entityManager.createQuery("select a from ArtifactTree a " +
                "where " +
                "a.task = :task and " +
                "(:queryAgents = false or a.uploadedBy in :agents) " +
                "order by a.time")
                .setParameter("task", task.asHistorical())
                .setParameter("queryAgents", agents != null)
                .setParameter("agents", agents == null || agents.isEmpty() ? dummyList() : agents)
                .getResultList();
    }
}
