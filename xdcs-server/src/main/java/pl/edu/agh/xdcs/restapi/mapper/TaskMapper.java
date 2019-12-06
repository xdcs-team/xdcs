package pl.edu.agh.xdcs.restapi.mapper;

import com.google.common.collect.ImmutableMap;
import pl.edu.agh.xdcs.db.entity.Task;
import pl.edu.agh.xdcs.mapper.EnumMapper;
import pl.edu.agh.xdcs.restapi.model.TaskDto;
import pl.edu.agh.xdcs.security.web.UserContext;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
public class TaskMapper {
    @Inject
    private UserContext userContext;
    @Inject
    private StateMapper stateMapper;

    public List<TaskDto> toRestEntities(Collection<Task> model) {
        return model.stream()
                .map(this::toRestEntity)
                .collect(Collectors.toList());
    }

    public TaskDto toRestEntity(Task model) {
        TaskDto dto = new TaskDto();
        dto.setId(model.getId());
        dto.setName(model.getName());
        dto.setState(stateMapper.map(model));
        dto.setDeploymentId(model.getDeploymentDescriptor().getDeploymentRef().getReferencedObjectId());
        dto.setTimeCreated(model.getTimeCreated().atOffset(userContext.getCurrentZoneOffset()));
        return dto;
    }
}
