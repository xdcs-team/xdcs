package pl.edu.agh.xdcs.agentapi.impl;

import io.grpc.stub.StreamObserver;
import pl.edu.agh.xdcs.api.Logs;
import pl.edu.agh.xdcs.api.OkResponse;
import pl.edu.agh.xdcs.api.TaskReportingGrpc;
import pl.edu.agh.xdcs.api.TaskResultReport;
import pl.edu.agh.xdcs.grpc.Service;
import pl.edu.agh.xdcs.util.ExponentialBackoff;

import javax.inject.Inject;
import java.time.Duration;

/**
 * @author Kamil Jarosz
 */
@Service
public class TaskReportingGrpcImpl extends TaskReportingGrpc.TaskReportingImplBase {
    @Inject
    private TaskReportingService taskReportingService;

    @Override
    public void uploadLogs(Logs request, StreamObserver<OkResponse> responseObserver) {
        taskReportingService.uploadLogs(request, responseObserver);
        responseObserver.onNext(OkResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void reportTaskResult(TaskResultReport request, StreamObserver<OkResponse> responseObserver) {
        ExponentialBackoff.fromPersistenceException(() -> {
            taskReportingService.reportTaskResult(request, responseObserver);
        }, Duration.ofMinutes(2));

        responseObserver.onNext(OkResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
