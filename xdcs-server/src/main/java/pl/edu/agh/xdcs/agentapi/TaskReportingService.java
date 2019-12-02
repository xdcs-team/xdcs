package pl.edu.agh.xdcs.agentapi;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import pl.edu.agh.xdcs.agentapi.mapper.LogTypeMapper;
import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.api.Logs;
import pl.edu.agh.xdcs.api.OkResponse;
import pl.edu.agh.xdcs.api.TaskId;
import pl.edu.agh.xdcs.api.TaskReportingGrpc;
import pl.edu.agh.xdcs.db.entity.RuntimeTaskEntity;
import pl.edu.agh.xdcs.db.entity.Task;
import pl.edu.agh.xdcs.grpc.Service;
import pl.edu.agh.xdcs.services.TaskService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;

/**
 * @author Kamil Jarosz
 */
@Service
public class TaskReportingService extends TaskReportingGrpc.TaskReportingImplBase {
    @Inject
    private Logger logger;

    @Inject
    private Agent currentAgent;

    @Inject
    private TaskService taskService;

    @Inject
    private LogTypeMapper logTypeMapper;

    @Override
    public void reportCompletion(TaskId request, StreamObserver<OkResponse> responseObserver) {
        taskService.reportCompletion(request.getTaskId());
        responseObserver.onNext(OkResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void uploadLogs(Logs request, StreamObserver<OkResponse> responseObserver) {
        saveLogLines(request.getTaskId(), request.getLinesList());
        responseObserver.onNext(OkResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    private void saveLogLines(String taskId, List<Logs.LogLine> linesList) {
        Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new RuntimeException("Runtime task not found: " + taskId));

        linesList.forEach(line -> {
            logger.debug(currentAgent.getName() + " logged for task " + taskId +
                    ": " + line.getContents().toStringUtf8());
            Timestamp ts = line.getTimestamp();
            taskService.saveLog(
                    task,
                    Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()),
                    logTypeMapper.toModelEntity(line.getType()),
                    line.getContents().toByteArray());
        });
    }
}
