package pl.edu.agh.xdcs.restapi.mapper.impl;

import com.google.common.collect.ImmutableMap;
import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;
import pl.edu.agh.xdcs.db.entity.TaskType;
import pl.edu.agh.xdcs.or.types.Deployment;
import pl.edu.agh.xdcs.or.types.Deployment.ConfigType;
import pl.edu.agh.xdcs.restapi.mapper.EnumMapper;
import pl.edu.agh.xdcs.restapi.model.DeploymentConfigDto;
import pl.edu.agh.xdcs.restapi.model.DeploymentConfigDto.TypeEnum;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
public class DeploymentConfigMapper {

    private EnumMapper<TaskType, TypeEnum> typeMapper =
            EnumMapper.forMapping(ImmutableMap.<TaskType, TypeEnum>builder()
                    .put(TaskType.CUDA, TypeEnum.CUDA)
                    .put(TaskType.OPENCL, TypeEnum.OPENCL)
                    .put(TaskType.DOCKER, TypeEnum.DOCKER)
                    .put(TaskType.SCRIPT, TypeEnum.SCRIPT)
                    .build());

    private EnumMapper<Deployment.ConfigType, TypeEnum> deploymentTypeMapper =
            EnumMapper.forMapping(ImmutableMap.<ConfigType, TypeEnum>builder()
                    .put(ConfigType.CUDA, TypeEnum.CUDA)
                    .put(ConfigType.OPENCL, TypeEnum.OPENCL)
                    .put(ConfigType.DOCKER, TypeEnum.DOCKER)
                    .put(ConfigType.SCRIPT, TypeEnum.SCRIPT)
                    .build());

    @Inject
    private KernelParamsMapper kernelParamsMapper;

    public DeploymentConfigDto toRestEntity(TaskDefinitionEntity model) {
        DeploymentConfigDto dto = new DeploymentConfigDto();
        dto.setType(typeMapper.toRestEntity(model.getType()));
        dto.setDockerfile(model.getDockerfile());
        dto.setKernelfile(model.getKernelFile());
        dto.setKernelname(model.getKernelName());
        dto.setKernelparams(kernelParamsMapper.toRestEntities(model.getKernelParams()));
        return dto;
    }

    public DeploymentConfigDto toRestEntity(Deployment.Config model) {
        DeploymentConfigDto deploymentConfigDto = new DeploymentConfigDto();
        deploymentConfigDto.setType(deploymentTypeMapper.toRestEntity(model.getType()));
        deploymentConfigDto.setDockerfile(model.getDockerfile());
        deploymentConfigDto.setScriptfile(model.getScriptfile());
        deploymentConfigDto.setKernelfile(model.getKernelfile());
        deploymentConfigDto.setKernelname(model.getKernelname());
        deploymentConfigDto.setKernelparams(Optional.ofNullable(model.getKernelParams())
                .map(kernelParamsMapper::toRestEntity)
                .orElse(null));
        return deploymentConfigDto;
    }

    public void updateModelEntity(DeploymentConfigDto rest, TaskDefinitionEntity model) {
        if (rest.getType() != null) {
            model.setType(typeMapper.toModelEntity(rest.getType()));
        }

        if (rest.getDockerfile() != null) {
            model.setDockerfile(rest.getDockerfile());
        }

        if (rest.getScriptfile() != null) {
            model.setScriptPath(rest.getScriptfile());
        }

        if (rest.getKernelfile() != null) {
            model.setKernelFile(rest.getKernelfile());
        }

        if (rest.getKernelname() != null) {
            model.setKernelName(rest.getKernelname());
        }

        if (rest.getKernelparams() != null) {
            model.setKernelParams(kernelParamsMapper.toModelEntity(rest.getKernelparams()));
        }
    }
}
