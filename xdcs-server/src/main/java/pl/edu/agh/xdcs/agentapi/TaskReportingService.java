package pl.edu.agh.xdcs.agentapi;

import io.grpc.stub.StreamObserver;
import pl.edu.agh.xdcs.api.OkResponse;
import pl.edu.agh.xdcs.api.TaskId;
import pl.edu.agh.xdcs.api.TaskReportingGrpc;
import pl.edu.agh.xdcs.grpc.Service;
import pl.edu.agh.xdcs.services.TaskService;

import javax.inject.Inject;

/**
 * @author Kamil Jarosz
 */
@Service
public class TaskReportingService extends TaskReportingGrpc.TaskReportingImplBase {
    @Inject
    private TaskService taskService;

    @Override
    public void reportCompletion(TaskId request, StreamObserver<OkResponse> responseObserver) {
        taskService.reportCompletion(request.getTaskId());
        responseObserver.onNext(OkResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
