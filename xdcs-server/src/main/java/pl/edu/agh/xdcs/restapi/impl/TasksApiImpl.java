package pl.edu.agh.xdcs.restapi.impl;

import pl.edu.agh.xdcs.db.entity.LogLineEntity;
import pl.edu.agh.xdcs.db.entity.ObjectRefEntity;
import pl.edu.agh.xdcs.db.entity.QueuedTaskEntity;
import pl.edu.agh.xdcs.db.entity.ResourcePatternEntity;
import pl.edu.agh.xdcs.db.entity.Task;
import pl.edu.agh.xdcs.or.ObjectRepository;
import pl.edu.agh.xdcs.or.types.Tree;
import pl.edu.agh.xdcs.or.util.ObjectRepositoryUtils;
import pl.edu.agh.xdcs.restapi.TasksApi;
import pl.edu.agh.xdcs.restapi.mapper.AgentEntityMapper;
import pl.edu.agh.xdcs.restapi.mapper.LogLineMapper;
import pl.edu.agh.xdcs.restapi.mapper.ResourcePatternMapper;
import pl.edu.agh.xdcs.restapi.mapper.ResourceTypeMapper;
import pl.edu.agh.xdcs.restapi.mapper.TaskMapper;
import pl.edu.agh.xdcs.restapi.model.ArtifactDto;
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
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
    private UriResolver resolver;

    @Inject
    private TaskMapper taskMapper;

    @Inject
    private ResourcePatternMapper resourcePatternMapper;

    @Inject
    private LogLineMapper logLineMapper;

    @Inject
    private WsUriResolver wsUriResolver;

    @Inject
    private AgentEntityMapper agentEntityMapper;

    @Inject
    private ObjectRepositoryUtils objectRepositoryUtils;

    @Inject
    private ObjectRepository objectRepository;

    @Context
    private UriInfo uriInfo;

    @Override
    public Response getTask(String taskId) {
        Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        return Response.ok(taskMapper.toRestEntity(task)).build();
    }

    @Override
    public Response getTaskArtifactContent(String taskId, String path) {
        Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        String artifactTree = task.asHistorical().getArtifactTree().getReferencedObjectId();
        Tree.Entry artifactEntry = objectRepositoryUtils.getChildEntry(artifactTree, path)
                .orElseThrow(() -> new NotFoundException("Artifact not found"));
        return Response.ok(objectRepository.cat(artifactEntry.getObjectId()))
                .header("Content-Disposition", "attachment; filename=\"" + artifactEntry.getName() + "\"")
                .build();
    }

    @Override
    public Response getTaskArtifacts(String taskId) {
        Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        ObjectRefEntity artifactTreeRef = task.asHistorical().getArtifactTree();
        if (artifactTreeRef == null) {
            return Response.ok(Collections.emptyList()).build();
        }

        String artifactTree = artifactTreeRef.getReferencedObjectId();
        List<ArtifactDto> artifacts = new ArrayList<>();
        objectRepositoryUtils.walkTree(artifactTree, (path, entry) -> {
            artifacts.add(new ArtifactDto()
                    .path(path)
                    .href(resolver.of(TasksApi::getTaskArtifactContent, taskId) +
                            "?path=" + resolver.escapeQueryParam(path)));
        });
        return Response.ok(artifacts).build();
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
    public Response getTaskLogs(String taskId, @NotNull Boolean queryAgents, OffsetDateTime from, OffsetDateTime to, List<String> agents) {
        Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));
        LogsDto logs = getLogs(taskId, from, to, agents, queryAgents, task);
        return Response.ok(logs).build();
    }

    private LogsDto getLogs(String taskId, OffsetDateTime from, OffsetDateTime to, List<String> agents, boolean queryAgents, Task task) {
        LogsDto logs = new LogsDto();
        logs.setWebsocketUrl(wsUriResolver.of(LogsWebSocket.class, uriInfo, taskId).toString());
        List<LogLineEntity> lines = getLogLines(queryAgents, agents, task, from, to);
        if (lines != null) {
            logs.setItems(logLineMapper.toRestEntities(lines));
        }
        return logs;
    }

    private List<LogLineEntity> getLogLines(boolean queryAgents, List<String> agents, Task task, OffsetDateTime from, OffsetDateTime to) {
        if (queryAgents) {
            return getLogLines(task, agents);
        }
        return getLogLines(task, from, to);
    }

    private List<LogLineEntity> getLogLines(Task task, List<String> agents) {
        if (agents.isEmpty()) {
            return null;
        }
        return taskService.getLogs(task, agentEntityMapper.toAgentEntities(agents));
    }

    private List<LogLineEntity> getLogLines(Task task, OffsetDateTime from, OffsetDateTime to) {
        return taskService.getLogs(
                task,
                from == null ? null : from.toInstant(),
                to == null ? null : to.toInstant());
    }

    @Override
    public Response getTasks(BigDecimal fromParam, BigDecimal maxResultsParam) {
        int from = fromParam == null ? 0 : fromParam.intValue();
        int maxResults = maxResultsParam == null ? Integer.MAX_VALUE : maxResultsParam.intValue();

        List<Task> tasks = taskService.queryTasks(from, maxResults);
        return prepareResponse(from, tasks);
    }

    @Override
    public Response getActiveTasks(BigDecimal fromParam, BigDecimal maxResultsParam) {
        int from = fromParam == null ? 0 : fromParam.intValue();
        int maxResults = maxResultsParam == null ? Integer.MAX_VALUE : maxResultsParam.intValue();

        List<Task> tasks = taskService.queryActiveTasks(from, maxResults);
        return prepareResponse(from, tasks);
    }

    private Response prepareResponse(int from, List<Task> tasks) {
        List<TaskDto> items = taskMapper.toRestEntities(tasks);
        return Response.ok(new TasksDto()
                .items(items)
                .from(from)
                .total(Math.toIntExact(taskService.countTasks()))).build();
    }

    @Override
    @SweepAfter(message = "after task started by REST")
    public Response startTask(TaskCreationDto taskCreation) {
        if (taskCreation.getName().isEmpty()) {
            throw RestUtils.throwBadRequest("Task name is empty");
        }

        if (taskCreation.getDeploymentId().isEmpty()) {
            throw RestUtils.throwBadRequest("Deployment ID is empty");
        }

        TaskService.TaskCreationWizard taskCreationWizard = taskService.newTask()
                .name(taskCreation.getName())
                .deploymentId(taskCreation.getDeploymentId());

        if (taskCreation.getResources().isEmpty()) {
            return RestUtils.badRequest("No resources");
        }

        taskCreation.getResources().forEach(res -> {
            if (res.getType() != null) {
                throw RestUtils.throwBadRequest("Cannot select type");
            }

            if (res.getAgent().isEmpty()) {
                throw RestUtils.throwBadRequest("Agent pattern is empty");
            }

            if (res.getKey().isEmpty()) {
                throw RestUtils.throwBadRequest("Resource key pattern is empty");
            }

            WildcardPattern agentPattern = WildcardPattern.parse(res.getAgent());
            WildcardPattern keyPattern = WildcardPattern.parse(res.getKey());
            taskCreationWizard.addResourcePattern(agentPattern, keyPattern);
        });

        QueuedTaskEntity task = taskCreationWizard.enqueue();
        return RestUtils.created(resolver.of(TasksApi::getTask, task.getId()));
    }
}
