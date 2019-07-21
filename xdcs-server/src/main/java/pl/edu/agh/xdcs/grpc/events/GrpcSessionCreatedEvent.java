package pl.edu.agh.xdcs.grpc.events;

import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.xdcs.grpc.ee.ManagedGrpcSession;

/**
 * @author Kamil Jarosz
 */
@Getter
@Builder
public class GrpcSessionCreatedEvent {
    private ManagedGrpcSession session;
}
