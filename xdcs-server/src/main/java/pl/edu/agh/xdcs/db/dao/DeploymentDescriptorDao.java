package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.entity.DeploymentDescriptorEntity;

import java.util.List;

/**
 * @author Kamil Jarosz
 */
public class DeploymentDescriptorDao extends DaoBase<DeploymentDescriptorEntity> {
    @Override
    protected Class<DeploymentDescriptorEntity> getEntityClass() {
        return DeploymentDescriptorEntity.class;
    }

    @SuppressWarnings("unchecked")
    public List<DeploymentDescriptorEntity> findByDefinitionId(String taskDefinitionId) {
        return entityManager
                .createQuery("select d from Deployment as d where d.definition.id = :definitionId")
                .setParameter("definitionId", taskDefinitionId)
                .getResultList();
    }
}
