package pl.edu.agh.xdcs.restapi.mapper.impl;

import pl.edu.agh.xdcs.db.entity.DeploymentDescriptorEntity;
import pl.edu.agh.xdcs.or.types.Deployment;
import pl.edu.agh.xdcs.restapi.model.DeploymentDescriptorDto;
import pl.edu.agh.xdcs.restapi.model.DeploymentDto;
import pl.edu.agh.xdcs.security.web.UserContext;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
public class DeploymentMapper {

    @Inject
    private UserContext userContext;

    @Inject
    private DeploymentConfigMapper deploymentConfigMapper;

    public List<DeploymentDescriptorDto> toRestEntities(Collection<DeploymentDescriptorEntity> model) {
        return model.stream()
                .map(this::toRestEntity)
                .collect(Collectors.toList());
    }

    public DeploymentDescriptorDto toRestEntity(DeploymentDescriptorEntity model) {
        DeploymentDescriptorDto dto = new DeploymentDescriptorDto();
        dto.setId(model.getDeploymentRef().getReferencedObjectId());
        dto.setTaskDefinitionId(model.getDefinition().getId());
        dto.setDescription(model.getDescription());
        dto.setTimeDeployed(model.getTimeDeployed().atOffset(userContext.getCurrentZoneOffset()));
        return dto;
    }

    public DeploymentDto toRestEntity(Deployment model) {
        DeploymentDto deploymentDto = new DeploymentDto();
        deploymentDto.setTaskDefinitionId(model.getDefinitionId());
        deploymentDto.setConfig(deploymentConfigMapper.toRestEntity(model.getConfig()));
        return deploymentDto;
    }
}
