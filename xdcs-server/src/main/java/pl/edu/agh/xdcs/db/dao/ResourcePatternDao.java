package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.entity.ResourcePatternEntity;

import java.util.List;

/**
 * @author Kamil Jarosz
 */
public class ResourcePatternDao extends EntityDaoBase<ResourcePatternEntity> {
    @Override
    protected Class<ResourcePatternEntity> getEntityClass() {
        return ResourcePatternEntity.class;
    }

    @SuppressWarnings("unchecked")
    public List<ResourcePatternEntity> findForTask(String taskId) {
        return entityManager
                .createQuery("select rp from ResourcePattern rp " +
                        "where rp.requester.id = :taskId")
                .setParameter("taskId", taskId)
                .getResultList();
    }
}
