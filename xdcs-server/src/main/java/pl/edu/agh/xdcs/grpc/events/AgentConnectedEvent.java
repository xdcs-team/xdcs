package pl.edu.agh.xdcs.grpc.events;

import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.xdcs.grpc.ee.ManagedGrpcSession;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Event fired just when an agent has connected to the server.
 * {@link ManagedGrpcSession} hasn't been created yet.
 *
 * @author Kamil Jarosz
 */
@Getter
@Builder
public class AgentConnectedEvent {
    private InetAddress agentAddress;
    private InetSocketAddress tunnelEndpoint;
}
