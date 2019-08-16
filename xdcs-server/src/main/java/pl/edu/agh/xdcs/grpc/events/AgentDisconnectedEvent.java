package pl.edu.agh.xdcs.grpc.events;

import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.xdcs.grpc.session.GrpcSession;

import java.net.InetAddress;

/**
 * Event fired just when an agent has disconnected from the server.
 * {@link GrpcSession} hasn't been closed yet.
 *
 * @author Kamil Jarosz
 */
@Getter
@Builder
public class AgentDisconnectedEvent {
    private String agentName;
    private InetAddress agentAddress;
}
