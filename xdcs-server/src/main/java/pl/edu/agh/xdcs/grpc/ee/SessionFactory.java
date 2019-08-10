package pl.edu.agh.xdcs.grpc.ee;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pl.edu.agh.xdcs.grpc.events.AgentConnectedEvent;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import java.net.InetSocketAddress;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class SessionFactory {
    @Resource
    private ManagedExecutorService executorService;

    public ManagedGrpcSession newManagedSession(AgentConnectedEvent event) {
        return ManagedGrpcSession.builder()
                .tunnelEndpoint(event.getTunnelEndpoint())
                .agentAddress(event.getAgentAddress())
                .agentName(event.getAgentName())
                .channel(createChannel(event.getTunnelEndpoint()))
                .build();
    }

    private ManagedChannel createChannel(InetSocketAddress target) {
        return ManagedChannelBuilder.forAddress(target.getHostString(), target.getPort())
                .executor(executorService)
                .usePlaintext()
                .build();
    }
}
