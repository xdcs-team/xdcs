package pl.edu.agh.xdcs.restapi.mapper.impl;

import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;
import pl.edu.agh.xdcs.restapi.mapper.SimpleMapper;
import pl.edu.agh.xdcs.restapi.model.TaskDefinitionDto;

/**
 * @author Kamil Jarosz
 */
public class TaskDefinitionMapper implements SimpleMapper<TaskDefinitionEntity, TaskDefinitionDto> {
    @Override
    public TaskDefinitionEntity toModelEntity(TaskDefinitionDto rest) {
        return TaskDefinitionEntity.builder()
                .name(rest.getName())
                .build();
    }

    @Override
    public TaskDefinitionDto toRestEntity(TaskDefinitionEntity model) {
        TaskDefinitionDto dto = new TaskDefinitionDto();
        dto.setId(model.getId());
        dto.setName(model.getName());
        return dto;
    }
}
