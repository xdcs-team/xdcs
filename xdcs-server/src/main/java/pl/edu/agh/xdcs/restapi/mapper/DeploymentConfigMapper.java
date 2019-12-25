package pl.edu.agh.xdcs.restapi.mapper;

import com.google.common.collect.ImmutableMap;
import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;
import pl.edu.agh.xdcs.db.entity.TaskType;
import pl.edu.agh.xdcs.mapper.EnumMapper;
import pl.edu.agh.xdcs.or.types.Deployment;
import pl.edu.agh.xdcs.or.types.Deployment.ConfigType;
import pl.edu.agh.xdcs.restapi.model.DeploymentConfigDto;
import pl.edu.agh.xdcs.restapi.model.DeploymentConfigDto.TypeEnum;
import pl.edu.agh.xdcs.restapi.model.TaskDefinitionDto;

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
        dto.setType(typeMapper.toApiEntity(model.getType()));
        dto.setDockerfile(model.getDockerfile());
        dto.setScriptfile(model.getScriptPath());
        dto.setAllocatePseudoTty(model.getAllocatePseudoTty());
        dto.setKernelfile(model.getKernelFile());
        dto.setKernelname(model.getKernelName());
        dto.setKernelparams(kernelParamsMapper.toRestEntities(model.getKernelParams()));
        dto.setArtifacts(model.getArtifacts());
        dto.setMergingScript(model.getMergingScript());
        return dto;
    }

    public DeploymentConfigDto toRestEntity(Deployment.Config model) {
        DeploymentConfigDto deploymentConfigDto = new DeploymentConfigDto();
        deploymentConfigDto.setType(deploymentTypeMapper.toApiEntity(model.getType()));
        deploymentConfigDto.setDockerfile(model.getDockerfile());
        deploymentConfigDto.setAllocatePseudoTty(model.getAllocatePseudoTty());
        deploymentConfigDto.setScriptfile(model.getScriptFile());
        deploymentConfigDto.setKernelfile(model.getKernelFile());
        deploymentConfigDto.setKernelname(model.getKernelName());
        deploymentConfigDto.setKernelparams(Optional.ofNullable(model.getKernelParams())
                .map(kernelParamsMapper::toRestEntity)
                .orElse(null));
        deploymentConfigDto.setArtifacts(model.getArtifacts());
        deploymentConfigDto.setMergingScript(model.getMergingScript());
        return deploymentConfigDto;
    }

    public void updateModelEntity(TaskDefinitionDto rest, TaskDefinitionEntity model) {
        model.setName(rest.getName());
        updateConfig(rest.getConfig(), model);
    }

    private void updateConfig(DeploymentConfigDto config, TaskDefinitionEntity model) {
        if (config == null) {
            return;
        }

        if (config.getType() != null) {
            model.setType(typeMapper.toModelEntity(config.getType()));
        }

        if (config.getDockerfile() != null) {
            model.setDockerfile(config.getDockerfile());
        }

        if (config.getAllocatePseudoTty() != null) {
            model.setAllocatePseudoTty(config.getAllocatePseudoTty());
        }

        if (config.getScriptfile() != null) {
            model.setScriptPath(config.getScriptfile());
        }

        if (config.getKernelfile() != null) {
            model.setKernelFile(config.getKernelfile());
        }

        if (config.getKernelname() != null) {
            model.setKernelName(config.getKernelname());
        }

        if (config.getKernelparams() != null) {
            model.setKernelParams(kernelParamsMapper.toModelEntity(config.getKernelparams()));
        }

        if (config.getArtifacts() != null) {
            model.setArtifacts(config.getArtifacts());
        }

        if (config.getMergingScript() != null) {
            model.setMergingScript(config.getMergingScript());
        }
    }
}
