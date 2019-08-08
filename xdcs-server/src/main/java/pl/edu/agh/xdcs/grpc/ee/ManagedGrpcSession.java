package pl.edu.agh.xdcs.grpc.ee;

import io.grpc.ManagedChannel;
import lombok.Builder;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * @author Kamil Jarosz
 */
@Builder
public class ManagedGrpcSession implements AutoCloseable {
    private final String sessionId = UUID.randomUUID().toString();
    private final InetSocketAddress tunnelEndpoint;
    private final InetAddress agentAddress;
    private final String agentName;
    private final ManagedChannel channel;

    public String getSessionId() {
        return sessionId;
    }

    public InetSocketAddress getTunnelEndpoint() {
        return tunnelEndpoint;
    }

    public InetAddress getAgentAddress() {
        return agentAddress;
    }

    public String getAgentName() {
        return agentName;
    }

    public ManagedChannel getChannel() {
        return channel;
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.shutdownNow();
        }
    }
}
