package pl.edu.agh.xdcs.agentapi.impl;

import com.google.common.base.Strings;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import pl.edu.agh.xdcs.agentapi.mapper.LogTypeMapper;
import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.api.Logs;
import pl.edu.agh.xdcs.api.OkResponse;
import pl.edu.agh.xdcs.api.TaskResultReport;
import pl.edu.agh.xdcs.db.DatabaseInconsistencyException;
import pl.edu.agh.xdcs.db.dao.AgentDao;
import pl.edu.agh.xdcs.db.entity.AgentEntity;
import pl.edu.agh.xdcs.db.entity.Task;
import pl.edu.agh.xdcs.mapper.UnsatisfiedMappingException;
import pl.edu.agh.xdcs.services.TaskService;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;

/**
 * @author Kamil Jarosz
 */
@Transactional
public class TaskReportingService {
    @Inject
    private Logger logger;

    @Inject
    private Agent currentAgent;

    @Inject
    private TaskService taskService;

    @Inject
    private LogTypeMapper logTypeMapper;

    @Inject
    private AgentDao agentDao;


    public void uploadLogs(Logs request, StreamObserver<OkResponse> responseObserver) {
        saveLogLines(request.getTaskId(), request.getLinesList());
    }

    private void saveLogLines(String taskId, List<Logs.LogLine> linesList) {
        Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new RuntimeException("Runtime task not found: " + taskId));

        AgentEntity currentAgentEntity = getCurrentAgentEntity();

        linesList.forEach(line -> {
            logger.debug(currentAgent.getName() + " logged for task " + taskId +
                    ": " + line.getContents().toStringUtf8());
            Timestamp ts = line.getTimestamp();
            taskService.saveLog(
                    task,
                    Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()),
                    logTypeMapper.toModelEntity(line.getType()),
                    line.getContents().toByteArray(),
                    currentAgentEntity);
        });
    }

    public void reportTaskResult(TaskResultReport request, StreamObserver<OkResponse> responseObserver) {
        Task task = taskService.getTaskById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found: " + request.getTaskId()));
        AgentEntity currentAgentEntity = getCurrentAgentEntity();
        switch (request.getResult()) {
            case FAILED:
                taskService.finishTask(task, currentAgentEntity, Task.Result.ERRORED);
                break;

            case SUCCEEDED:
                taskService.finishTask(task, currentAgentEntity, Task.Result.FINISHED);
                break;

            default:
            case UNRECOGNIZED:
                throw new UnsatisfiedMappingException("Unknown response value");
        }

        if (!Strings.isNullOrEmpty(request.getArtifactTree())) {
            taskService.addArtifactTree(task, currentAgentEntity, request.getArtifactTree());
        }
    }

    private AgentEntity getCurrentAgentEntity() {
        return agentDao.findByName(currentAgent.getName())
                .orElseThrow(() -> new DatabaseInconsistencyException("Agent: " + currentAgent.getName() + " not found."));
    }
}
