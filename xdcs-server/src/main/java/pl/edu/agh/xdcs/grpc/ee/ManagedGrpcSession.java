package pl.edu.agh.xdcs.grpc.ee;

import io.grpc.ManagedChannel;
import lombok.Builder;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author Kamil Jarosz
 */
@Builder
public class ManagedGrpcSession implements AutoCloseable {
    private final InetSocketAddress tunnelEndpoint;
    private final InetAddress agentAddress;
    private final ManagedChannel channel;

    public InetSocketAddress getTunnelEndpoint() {
        return tunnelEndpoint;
    }

    public InetAddress getAgentAddress() {
        return agentAddress;
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
