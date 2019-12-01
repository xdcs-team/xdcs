package pl.edu.agh.xdcs.restapi.impl;

import pl.edu.agh.xdcs.db.entity.LogLineEntity;
import pl.edu.agh.xdcs.db.entity.QueuedTaskEntity;
import pl.edu.agh.xdcs.db.entity.ResourcePatternEntity;
import pl.edu.agh.xdcs.db.entity.ResourceType;
import pl.edu.agh.xdcs.db.entity.Task;
import pl.edu.agh.xdcs.restapi.TasksApi;
import pl.edu.agh.xdcs.restapi.mapper.LogLineMapper;
import pl.edu.agh.xdcs.restapi.mapper.ResourcePatternMapper;
import pl.edu.agh.xdcs.restapi.mapper.ResourceTypeMapper;
import pl.edu.agh.xdcs.restapi.mapper.TaskMapper;
import pl.edu.agh.xdcs.restapi.model.LogsDto;
import pl.edu.agh.xdcs.restapi.model.TaskConditionsDto;
import pl.edu.agh.xdcs.restapi.model.TaskCreationDto;
import pl.edu.agh.xdcs.restapi.model.TaskDto;
import pl.edu.agh.xdcs.restapi.model.TasksDto;
import pl.edu.agh.xdcs.restapi.util.RestUtils;
import pl.edu.agh.xdcs.services.TaskService;
import pl.edu.agh.xdcs.services.sweeper.SweepAfter;
import pl.edu.agh.xdcs.util.UriResolver;
import pl.edu.agh.xdcs.util.WildcardPattern;
import pl.edu.agh.xdcs.util.WsUriResolver;
import pl.edu.agh.xdcs.ws.impl.LogsWebSocket;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
@Transactional
public class TasksApiImpl implements TasksApi {
    @Inject
    private TaskService taskService;

    @Inject
    private ResourceTypeMapper resourceTypeMapper;

    @Inject
    private UriResolver resolver;

    @Inject
    private TaskMapper taskMapper;

    @Inject
    private ResourcePatternMapper resourcePatternMapper;

    @Inject
    private LogLineMapper logLineMapper;

    @Inject
    private WsUriResolver wsUriResolver;

    @Context
    private UriInfo uriInfo;

    @Override
    public Response getTask(String taskId) {
        Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        return Response.ok(taskMapper.toRestEntity(task)).build();
    }

    @Override
    public Response getTaskConditions(String taskId) {
        taskService.getTaskById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"))
                .asQueued()
                .orElseThrow(() -> new NotFoundException("Task is not queued"));
        List<ResourcePatternEntity> resourcePatterns = taskService.getResourcePatterns(taskId);
        return Response.ok(new TaskConditionsDto()
                .resources(resourcePatterns.stream()
                        .map(resourcePatternMapper::toRestEntity)
                        .collect(Collectors.toList())))
                .build();
    }

    @Override
    public Response getTaskLogs(String taskId, OffsetDateTime from, OffsetDateTime to) {
        Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));
        LogsDto logs = new LogsDto();
        logs.setWebsocketUrl(wsUriResolver.of(LogsWebSocket.class, uriInfo, taskId).toString());

        List<LogLineEntity> lines = taskService.getLogs(
                task,
                from == null ? null : from.toInstant(),
                to == null ? null : to.toInstant());

        logs.setItems(logLineMapper.toRestEntities(lines));
        return Response.ok(logs).build();
    }

    @Override
    public Response getTasks() {
        List<Task> tasks = taskService.getAllTasks();
        List<TaskDto> items = taskMapper.toRestEntities(tasks);
        return Response.ok(new TasksDto()
                .items(items)
                .from(0)
                .total(items.size())).build();
    }

    @Override
    @SweepAfter(message = "after task started by REST")
    public Response startTask(TaskCreationDto taskCreation) {
        TaskService.TaskCreationWizard taskCreationWizard = taskService.newTask()
                .name(taskCreation.getName())
                .deploymentId(taskCreation.getDeploymentId());

        if (taskCreation.getResources().isEmpty()) {
            return RestUtils.badRequest("No resources");
        }

        taskCreation.getResources().forEach(res -> {
            ResourceType type = resourceTypeMapper.toModelEntity(res.getType());
            WildcardPattern agentPattern = WildcardPattern.parse(res.getAgent());
            WildcardPattern keyPattern = WildcardPattern.parse(res.getKey());
            taskCreationWizard.addResourcePattern(type, agentPattern, keyPattern, res.getQuantity());
        });

        QueuedTaskEntity task = taskCreationWizard.enqueue();
        return RestUtils.created(resolver.of(TasksApi::getTask, task.getId()));
    }
}
