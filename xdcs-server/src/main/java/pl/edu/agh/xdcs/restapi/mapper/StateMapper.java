package pl.edu.agh.xdcs.restapi.mapper;

import com.google.common.collect.ImmutableMap;
import pl.edu.agh.xdcs.db.DatabaseInconsistencyException;
import pl.edu.agh.xdcs.db.entity.Task;
import pl.edu.agh.xdcs.mapper.EnumMapper;
import pl.edu.agh.xdcs.restapi.model.TaskDto;

/**
 * @author Krystian Życiński
 */
public class StateMapper {
    private EnumMapper<Task.Type, TaskDto.StateEnum> typeMapper = EnumMapper.forMapping(
            ImmutableMap.<Task.Type, TaskDto.StateEnum>builder()
                    .put(Task.Type.RUNTIME, TaskDto.StateEnum.IN_PROGRESS)
                    .put(Task.Type.QUEUED, TaskDto.StateEnum.QUEUED)
                    .build());

    private EnumMapper<Task.Result, TaskDto.StateEnum> resultMapper = EnumMapper.forMapping(
            ImmutableMap.<Task.Result, TaskDto.StateEnum>builder()
                    .put(Task.Result.FINISHED, TaskDto.StateEnum.FINISHED)
                    .put(Task.Result.ERRORED, TaskDto.StateEnum.ERRORED)
                    .put(Task.Result.CANCELED, TaskDto.StateEnum.CANCELED)
                    .build());

    public TaskDto.StateEnum map(Task task) {
        if (task.getType() != Task.Type.HISTORICAL)
            return typeMapper.toApiEntity(task.getType());

        return resultMapper.toApiEntity(task.getResult().orElseThrow(()
                -> new DatabaseInconsistencyException("Historical task without result")));
    }
}
