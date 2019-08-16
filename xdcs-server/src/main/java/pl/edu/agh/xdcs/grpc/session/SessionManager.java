package pl.edu.agh.xdcs.grpc.session;

import pl.edu.agh.xdcs.grpc.context.SessionContext;
import pl.edu.agh.xdcs.grpc.events.AgentConnectedEvent;
import pl.edu.agh.xdcs.grpc.events.AgentDisconnectedEvent;
import pl.edu.agh.xdcs.grpc.events.GrpcSessionClosedEvent;
import pl.edu.agh.xdcs.grpc.events.GrpcSessionCreatedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class SessionManager {
    private final Map<String, GrpcSession> sessionsByNames = new ConcurrentHashMap<>();

    @Inject
    private SessionContext sessionContext;

    @Inject
    private SessionFactory sessionFactory;

    @Inject
    private Event<GrpcSessionCreatedEvent> sessionCreatedEvent;

    @Inject
    private Event<GrpcSessionClosedEvent> sessionClosedEvent;

    public Optional<GrpcSession> getSession(String agentName) {
        return Optional.ofNullable(sessionsByNames.get(agentName));
    }

    public void createSession(@Observes AgentConnectedEvent agentConnectedEvent) {
        GrpcSession session = sessionFactory.newManagedSession(agentConnectedEvent);
        sessionsByNames.put(session.getAgentName(), session);

        GrpcSessionCreatedEvent event = GrpcSessionCreatedEvent.builder()
                .session(session)
                .build();
        sessionCreatedEvent.fire(event);
        sessionCreatedEvent.fireAsync(event);
    }

    public void sessionClosed(@Observes AgentDisconnectedEvent agentDisconnectedEvent) {
        getSession(agentDisconnectedEvent.getAgentName()).ifPresent(session -> {
            sessionContext.evict(session);
            sessionsByNames.remove(session.getAgentName());
            session.close();

            GrpcSessionClosedEvent event = GrpcSessionClosedEvent.builder()
                    .session(session)
                    .build();
            sessionClosedEvent.fire(event);
            sessionClosedEvent.fireAsync(event);
        });
    }
}
