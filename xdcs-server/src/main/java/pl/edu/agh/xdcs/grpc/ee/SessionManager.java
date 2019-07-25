package pl.edu.agh.xdcs.grpc.ee;

import pl.edu.agh.xdcs.grpc.context.SessionContext;
import pl.edu.agh.xdcs.grpc.events.AgentConnectedEvent;
import pl.edu.agh.xdcs.grpc.events.AgentDisconnectedEvent;
import pl.edu.agh.xdcs.grpc.events.GrpcSessionClosedEvent;
import pl.edu.agh.xdcs.grpc.events.GrpcSessionCreatedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class SessionManager {
    private final Map<InetAddress, ManagedGrpcSession> sessionsByAgentAddresses = new HashMap<>();

    @Inject
    private SessionContext sessionContext;

    @Inject
    private SessionFactory sessionFactory;

    @Inject
    private Event<GrpcSessionCreatedEvent> sessionCreatedEvent;

    @Inject
    private Event<GrpcSessionClosedEvent> sessionClosedEvent;

    public synchronized Optional<ManagedGrpcSession> getSession(SocketAddress clientAddress) {
        return getSession(((InetSocketAddress) clientAddress).getAddress());
    }

    public synchronized Optional<ManagedGrpcSession> getSession(InetAddress clientAddress) {
        return Optional.ofNullable(sessionsByAgentAddresses.get(clientAddress));
    }

    public void createSession(@Observes AgentConnectedEvent agentConnectedEvent) {
        ManagedGrpcSession session = sessionFactory.newManagedSession(agentConnectedEvent);
        sessionsByAgentAddresses.put(session.getAgentAddress(), session);

        GrpcSessionCreatedEvent event = GrpcSessionCreatedEvent.builder()
                .session(session)
                .build();
        sessionCreatedEvent.fire(event);
        sessionCreatedEvent.fireAsync(event);
    }

    public void sessionClosed(@Observes AgentDisconnectedEvent agentDisconnectedEvent) {
        getSession(agentDisconnectedEvent.getAgentAddress()).ifPresent(session -> {
            sessionContext.evict(session);
            sessionsByAgentAddresses.remove(session.getAgentAddress());
            session.close();

            GrpcSessionClosedEvent event = GrpcSessionClosedEvent.builder()
                    .session(session)
                    .build();
            sessionClosedEvent.fire(event);
            sessionClosedEvent.fireAsync(event);
        });
    }
}
