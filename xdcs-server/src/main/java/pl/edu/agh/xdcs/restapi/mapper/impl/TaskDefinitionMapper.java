package pl.edu.agh.xdcs.restapi.mapper.impl;

import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;
import pl.edu.agh.xdcs.restapi.model.TaskDefinitionDto;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
public class TaskDefinitionMapper {
    @Inject
    private TaskDefinitionConfigMapper configMapper;

    public List<TaskDefinitionDto> toRestEntities(Collection<TaskDefinitionEntity> model) {
        return model.stream()
                .map(this::toRestEntity)
                .collect(Collectors.toList());
    }

    public TaskDefinitionDto toRestEntity(TaskDefinitionEntity model) {
        TaskDefinitionDto dto = new TaskDefinitionDto();
        dto.setId(model.getId());
        dto.setName(model.getName());
        dto.setConfig(configMapper.toRestEntity(model));
        return dto;
    }
}
