package pl.edu.agh.xdcs.grpc.events;

import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.xdcs.grpc.session.GrpcSession;

/**
 * @author Kamil Jarosz
 */
@Getter
@Builder
public class GrpcSessionCreatedEvent {
    private GrpcSession session;
}
