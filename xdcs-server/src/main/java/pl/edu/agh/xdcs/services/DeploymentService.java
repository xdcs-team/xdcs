package pl.edu.agh.xdcs.services;

import pl.edu.agh.xdcs.db.dao.DeploymentDescriptorDao;
import pl.edu.agh.xdcs.db.entity.DeploymentDescriptorEntity;
import pl.edu.agh.xdcs.db.entity.ObjectRefEntity;
import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;
import pl.edu.agh.xdcs.or.ObjectRepository;
import pl.edu.agh.xdcs.or.types.Deployment;
import pl.edu.agh.xdcs.workspace.ObjectRepositoryWorkspaceWriter;
import pl.edu.agh.xdcs.workspace.Workspace;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Kamil Jarosz
 */
@Transactional
public class DeploymentService {
    @Inject
    private ObjectRepository objectRepository;

    @Inject
    private TaskDefinitionService definitionService;

    @Inject
    private DeploymentDescriptorDao deploymentDescriptorDao;

    public Deployment getDeployment(String deploymentId) {
        return objectRepository.cat(deploymentId, Deployment.class);
    }

    public Workspace getWorkspaceForDeployment(String deploymentId) {
        return Workspace.forObject(objectRepository, getDeployment(deploymentId).getRoot());
    }

    public String deploy(TaskDefinitionEntity definition, String description) {
        ObjectRepositoryWorkspaceWriter writer = ObjectRepositoryWorkspaceWriter.forObjectRepository(objectRepository);
        String root = writer.write(definitionService.getWorkspace(definition));
        Deployment deployment = Deployment.builder()
                .definitionId(definition.getId())
                .root(root)
                .build();

        String deploymentId = objectRepository.store(deployment);
        addDeploymentDescriptor(definition, deploymentId, description);
        return deploymentId;
    }

    private void addDeploymentDescriptor(TaskDefinitionEntity definition, String deploymentId, String description) {
        DeploymentDescriptorEntity desc = new DeploymentDescriptorEntity();
        desc.setDefinition(definition);
        desc.setDeploymentRef(new ObjectRefEntity(deploymentId, Deployment.class));
        desc.setDescription(description);
        deploymentDescriptorDao.persist(desc);
    }
}
