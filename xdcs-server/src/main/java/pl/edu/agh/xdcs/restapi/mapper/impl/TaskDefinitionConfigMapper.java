package pl.edu.agh.xdcs.restapi.mapper.impl;

import com.google.common.collect.ImmutableMap;
import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;
import pl.edu.agh.xdcs.db.entity.TaskType;
import pl.edu.agh.xdcs.restapi.mapper.EnumMapper;
import pl.edu.agh.xdcs.restapi.model.TaskDefinitionConfigDto;
import pl.edu.agh.xdcs.restapi.model.TaskDefinitionConfigDto.TypeEnum;

import javax.inject.Inject;

/**
 * @author Kamil Jarosz
 */
public class TaskDefinitionConfigMapper {
    private EnumMapper<TaskType, TypeEnum> typeMapper =
            EnumMapper.forMapping(ImmutableMap.<TaskType, TypeEnum>builder()
                    .put(TaskType.CUDA, TypeEnum.CUDA)
                    .put(TaskType.OPENCL, TypeEnum.OPENCL)
                    .put(TaskType.DOCKER, TypeEnum.DOCKER)
                    .put(TaskType.SCRIPT, TypeEnum.SCRIPT)
                    .build());

    @Inject
    private KernelParamsMapper kernelParamsMapper;

    public TaskDefinitionConfigDto toRestEntity(TaskDefinitionEntity model) {
        TaskDefinitionConfigDto dto = new TaskDefinitionConfigDto();
        dto.setName(model.getName());
        dto.setType(typeMapper.toRestEntity(model.getType()));
        dto.setDockerfile(model.getDockerfile());
        dto.setKernelfile(model.getKernelFile());
        dto.setKernelname(model.getKernelName());
        dto.setKernelparams(kernelParamsMapper.toRestEntity(model.getKernelParams()));
        return dto;
    }

    public void updateModelEntity(TaskDefinitionConfigDto rest, TaskDefinitionEntity model) {
        if (rest.getName() != null) {
            model.setName(rest.getName());
        }

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
