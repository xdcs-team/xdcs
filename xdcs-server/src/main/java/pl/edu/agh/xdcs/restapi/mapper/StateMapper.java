package pl.edu.agh.xdcs.restapi.mapper;

import com.google.common.collect.ImmutableMap;
import pl.edu.agh.xdcs.db.DatabaseInconsistencyException;
import pl.edu.agh.xdcs.db.entity.Task;
import pl.edu.agh.xdcs.mapper.EnumMapper;
import pl.edu.agh.xdcs.restapi.model.TaskDto;
import org.slf4j.Logger;
import pl.edu.agh.xdcs.restapi.model.TaskState;

import javax.inject.Inject;

/**
 * @author Krystian Życiński
 */
public class StateMapper {
    @Inject
    private Logger logger;

    private EnumMapper<Task.Type, TaskState> typeMapper = EnumMapper.forMapping(
            ImmutableMap.<Task.Type, TaskState>builder()
                    .put(Task.Type.RUNTIME, TaskState.IN_PROGRESS)
                    .put(Task.Type.QUEUED, TaskState.QUEUED)
                    .build());

    private EnumMapper<Task.Result, TaskState> resultMapper = EnumMapper.forMapping(
            ImmutableMap.<Task.Result, TaskState>builder()
                    .put(Task.Result.FINISHED, TaskState.FINISHED)
                    .put(Task.Result.ERRORED, TaskState.ERRORED)
                    .put(Task.Result.CANCELED, TaskState.CANCELED)
                    .build());

    public TaskState map(Task task) {
        if (task.getType() != Task.Type.HISTORICAL)
            return typeMapper.toApiEntity(task.getType());

        logger.error("Historical task without result name: " + task.getName() + ", id: " + task.getId());
        return task.getResult().map(resultMapper::toApiEntity)
                .orElse(TaskState.ERRORED);
    }
}
