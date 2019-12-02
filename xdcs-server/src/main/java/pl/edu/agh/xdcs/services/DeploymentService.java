package pl.edu.agh.xdcs.services;

import org.slf4j.Logger;
import pl.edu.agh.xdcs.db.dao.DeploymentDescriptorDao;
import pl.edu.agh.xdcs.db.entity.DeploymentDescriptorEntity;
import pl.edu.agh.xdcs.db.entity.KernelParameters;
import pl.edu.agh.xdcs.db.entity.ObjectRefEntity;
import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;
import pl.edu.agh.xdcs.db.entity.TaskType;
import pl.edu.agh.xdcs.or.ObjectRepository;
import pl.edu.agh.xdcs.or.types.Deployment;
import pl.edu.agh.xdcs.workspace.ObjectRepositoryWorkspaceWriter;
import pl.edu.agh.xdcs.workspace.Workspace;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
@Transactional
public class DeploymentService {
    @Inject
    private Logger logger;

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
        // write the workspace to the filesystem
        ObjectRepositoryWorkspaceWriter writer = ObjectRepositoryWorkspaceWriter.forObjectRepository(objectRepository);
        String root = writer.write(definitionService.getWorkspace(definition));

        DeploymentDescriptorEntity desc = new DeploymentDescriptorEntity();
        Deployment deployment = Deployment.builder()
                .definitionId(definition.getId())
                .descriptorId(desc.getId())
                .root(root)
                .config(buildDeploymentConfig(definition))
                .build();
        // store the deployment
        String deploymentId = objectRepository.store(deployment);

        desc.setDefinition(definition);
        desc.setDeploymentRef(ObjectRefEntity.of(deploymentId, Deployment.class));
        desc.setDescription(description);
        deploymentDescriptorDao.persist(desc);
        logger.info("Task definition " + definition.getId() + " deployed: " + deploymentId);
        return deploymentId;
    }

    private Deployment.Config buildDeploymentConfig(TaskDefinitionEntity definition) {
        return Deployment.Config.builder()
                .type(mapConfigType(definition.getType()))
                .dockerfile(definition.getDockerfile())
                .kernelFile(definition.getKernelFile())
                .kernelName(definition.getKernelName())
                .scriptFile(definition.getScriptPath())
                .kernelParams(Optional.ofNullable(definition.getKernelParams())
                        .map(KernelParameters::getParameters)
                        .orElse(null))
                .build();
    }

    private Deployment.ConfigType mapConfigType(TaskType type) {
        if (type == null) {
            throw new DeploymentFailedException("Unspecified type");
        }

        switch (type) {
            case OPENCL:
                return Deployment.ConfigType.OPENCL;
            case CUDA:
                return Deployment.ConfigType.CUDA;
            case DOCKER:
                return Deployment.ConfigType.DOCKER;
            case SCRIPT:
                return Deployment.ConfigType.SCRIPT;
            default:
                throw new DeploymentFailedException("Unknown type: " + type);
        }
    }
}
