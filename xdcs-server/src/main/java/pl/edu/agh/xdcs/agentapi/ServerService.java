package pl.edu.agh.xdcs.agentapi;

import com.google.protobuf.Any;
import io.grpc.stub.StreamObserver;
import pl.edu.agh.xdcs.api.AgentRegistrationRequest;
import pl.edu.agh.xdcs.api.AgentRegistrationResponse;
import pl.edu.agh.xdcs.api.ServerGrpc;
import pl.edu.agh.xdcs.api.Task;
import pl.edu.agh.xdcs.api.TaskType;
import pl.edu.agh.xdcs.grpc.AnyObserver;

/**
 * @author Kamil Jarosz
 */
public class ServerService extends ServerGrpc.ServerImplBase {
    @Override
    public void registerAgent(AgentRegistrationRequest request, StreamObserver<AgentRegistrationResponse> responseObserver) {
        responseObserver.onNext(AgentRegistrationResponse.newBuilder()
                .setSuccess(true)
                .setAny(Any.pack(Task.newBuilder()
                        .setType(TaskType.OPEN_CL)
                        .build()))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Any> taskExecution(StreamObserver<Any> responseObserver) {
        return AnyObserver.builder()
                .match(Task.class, task -> {
                    System.out.println(task);
                    responseObserver.onNext(Any.pack(Task.newBuilder().build()));
                })
                .build();
    }
}