package pl.edu.agh.xdcs.grpc;

import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import pl.edu.agh.xdcs.api.HeartbeatGrpc;
import pl.edu.agh.xdcs.api.HeartbeatRequest;
import pl.edu.agh.xdcs.grpc.events.GrpcSessionCreatedEvent;

import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import java.net.InetAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A bean used for health-checking the reverse-forwarded agent connection.
 *
 * @author Kamil Jarosz
 */
public class HeartbeatCheck {
    @Inject
    private Logger logger;

    public void checkHeartbeat(@ObservesAsync GrpcSessionCreatedEvent event) {
        ManagedChannel channel = event.getSession().getChannel();
        InetAddress agentAddress = event.getSession().getAgentAddress();

        HeartbeatGrpc.HeartbeatFutureStub heartbeat = HeartbeatGrpc.newFutureStub(channel);
        logger.debug("Agent heartbeat check for " + agentAddress);
        try {
            heartbeat.heartbeat(HeartbeatRequest.newBuilder().build())
                    .get(10, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException | TimeoutException e) {
            String message = "Agent hasn't responded to health check";
            logger.error(message);
            throw new RuntimeException(message, e);
        }

        logger.debug("Agent heartbeat check for " + agentAddress + " succeeded");
    }
}
