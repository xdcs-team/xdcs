package pl.edu.agh.xdcs.agentapi;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import pl.edu.agh.xdcs.api.HeartbeatGrpc;
import pl.edu.agh.xdcs.api.HeartbeatRequest;
import pl.edu.agh.xdcs.api.HeartbeatResponse;
import pl.edu.agh.xdcs.grpc.Service;

import javax.inject.Inject;

/**
 * @author Kamil Jarosz
 */
@Service
public class HeartbeatService extends HeartbeatGrpc.HeartbeatImplBase {
    @Inject
    private Logger logger;

    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        logger.debug("Received heartbeat request");
        responseObserver.onNext(HeartbeatResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
