package pl.edu.agh.xdcs.grpc.ee;

import pl.edu.agh.xdcs.api.HeartbeatGrpc;
import pl.edu.agh.xdcs.grpc.scope.SessionScoped;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Kamil Jarosz
 */
@SessionScoped
public class StubProducer {
    @Inject
    private ManagedGrpcSession session;

    @Produces
    public HeartbeatGrpc.HeartbeatBlockingStub getHeartbeatBlockingStub() {
        return HeartbeatGrpc.newBlockingStub(session.getChannel());
    }

    @Produces
    public HeartbeatGrpc.HeartbeatStub getHeartbeatStub() {
        return HeartbeatGrpc.newStub(session.getChannel());
    }

    @Produces
    public HeartbeatGrpc.HeartbeatFutureStub getHeartbeatFutureStub() {
        return HeartbeatGrpc.newFutureStub(session.getChannel());
    }
}
