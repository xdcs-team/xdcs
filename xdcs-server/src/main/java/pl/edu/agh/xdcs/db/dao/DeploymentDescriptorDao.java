package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.entity.DeploymentDescriptorEntity;
import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;

import java.util.List;

/**
 * @author Kamil Jarosz
 */
public class DeploymentDescriptorDao extends DaoBase<DeploymentDescriptorEntity> {
    @Override
    protected Class<DeploymentDescriptorEntity> getEntityClass() {
        return DeploymentDescriptorEntity.class;
    }
}
