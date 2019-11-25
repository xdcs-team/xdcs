package pl.edu.agh.xdcs.restapi.mapper.impl;

import com.google.common.collect.ImmutableMap;
import pl.edu.agh.xdcs.db.entity.Task;
import pl.edu.agh.xdcs.restapi.mapper.EnumMapper;
import pl.edu.agh.xdcs.restapi.model.TaskDto;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
public class TaskMapper {
    private EnumMapper<Task.Type, TaskDto.StateEnum> stateMapper = EnumMapper.forMapping(
            ImmutableMap.<Task.Type, TaskDto.StateEnum>builder()
                    .put(Task.Type.RUNTIME, TaskDto.StateEnum.IN_PROGRESS)
                    .put(Task.Type.HISTORICAL, TaskDto.StateEnum.FINISHED)
                    .put(Task.Type.QUEUED, TaskDto.StateEnum.QUEUED)
                    .build());

    public List<TaskDto> toRestEntities(Collection<Task> model) {
        return model.stream()
                .map(this::toRestEntity)
                .collect(Collectors.toList());
    }

    public TaskDto toRestEntity(Task model) {
        TaskDto dto = new TaskDto();
        dto.setId(model.getId());
        dto.setName(model.getName());
        dto.setState(stateMapper.toRestEntity(model.getType()));
        dto.setDeploymentId(model.getDeploymentDescriptor().getDeploymentRef().getReferencedObjectId());
        return dto;
    }
}
